package vn.io.oldmoon.shopizer.user.business.event;

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
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;

@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherTest {

  @Mock private RabbitTemplate rabbitTemplate;

  @InjectMocks private RabbitMqEventPublisher eventPublisher;

  @Test
  @DisplayName("publish should delegate to RabbitTemplate when RabbitMqEvent is valid")
  void publish_WhenEventIsNotNull_ShouldSendToRabbitTemplate() {
    // Given
    Object event = new Object();
    String routingKey = "user.created.key";
    RabbitMqEvent rabbitMqEvent = new RabbitMqEvent(event, routingKey);

    // When
    eventPublisher.publish(rabbitMqEvent);

    // Then
    verify(rabbitTemplate).convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);
  }

  @Test
  @DisplayName("publish should do nothing when RabbitMqEvent or event payload is null")
  void publish_WhenEventIsNull_ShouldNotSendToRabbitTemplate() {
    // Given
    String routingKey = "user.created.key";

    // When
    eventPublisher.publish(null);
    eventPublisher.publish(new RabbitMqEvent(null, routingKey));

    // Then
    verifyNoInteractions(rabbitTemplate);
  }

  @Test
  @DisplayName("publish should catch and log exception when RabbitTemplate fails after commit")
  void publish_WhenRabbitTemplateThrowsException_ShouldNotPropagate() {
    Object event = new Object();
    String routingKey = "user.created.key";
    RabbitMqEvent rabbitMqEvent = new RabbitMqEvent(event, routingKey);

    doThrow(new RuntimeException("Broker unavailable"))
        .when(rabbitTemplate)
        .convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    assertThatCode(() -> eventPublisher.publish(rabbitMqEvent)).doesNotThrowAnyException();
  }
}
