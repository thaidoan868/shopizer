package vn.io.oldmoon.shopizer.product.business.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import vn.io.oldmoon.shopizer.common.core.event.RabbitMqEvent;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;

@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherTest {

  @Mock private RabbitTemplate rabbitTemplate;

  @InjectMocks private RabbitMqEventPublisher eventPublisher;

  @Test
  @DisplayName("publish should send message via RabbitTemplate when RabbitMqEvent is valid")
  void publish_WhenRabbitMqEventIsValid_ShouldSendToRabbitTemplate() {
    Object payload = "sample-event";
    String routingKey = RabbitMqConfig.videoProcessingBindingKey;
    RabbitMqEvent rabbitMqEvent = new RabbitMqEvent(payload, routingKey);

    eventPublisher.publish(rabbitMqEvent);

    verify(rabbitTemplate)
        .convertAndSend(RabbitMqConfig.productEventExchange, routingKey, payload);
  }

  @Test
  @DisplayName("publish should skip when RabbitMqEvent or its payload is null")
  void publish_WhenRabbitMqEventOrPayloadIsNull_ShouldDoNothing() {
    eventPublisher.publish(null);
    eventPublisher.publish(new RabbitMqEvent(null, "routing.key"));

    verifyNoInteractions(rabbitTemplate);
  }

  @Test
  @DisplayName("publish should catch and log exception when RabbitTemplate fails after commit")
  void publish_WhenRabbitTemplateThrowsException_ShouldNotPropagate() {
    Object payload = "sample-event";
    String routingKey = RabbitMqConfig.videoProcessingBindingKey;
    RabbitMqEvent rabbitMqEvent = new RabbitMqEvent(payload, routingKey);

    doThrow(new RuntimeException("Broker unavailable"))
        .when(rabbitTemplate)
        .convertAndSend(RabbitMqConfig.productEventExchange, routingKey, payload);

    assertThatCode(() -> eventPublisher.publish(rabbitMqEvent)).doesNotThrowAnyException();
  }
}
