package vn.io.oldmoon.shopizer.user.business.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;

@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherTest {

  @Mock private RabbitTemplate rabbitTemplate;

  @InjectMocks private RabbitMqEventPublisher eventPublisher;

  @Test
  @DisplayName("publish should delegate to RabbitTemplate when event is valid")
  void publish_WhenEventIsNotNull_ShouldSendToRabbitTemplate() {
    // Given
    Object event = new Object();
    String routingKey = "user.created.key";

    // When
    eventPublisher.publish(event, routingKey);

    // Then
    verify(rabbitTemplate).convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);
  }

  @Test
  @DisplayName("publish should do nothing when event is null")
  void publish_WhenEventIsNull_ShouldNotSendToRabbitTemplate() {
    // Given
    String routingKey = "user.created.key";

    // When
    eventPublisher.publish(null, routingKey);

    // Then
    verifyNoInteractions(rabbitTemplate);
  }
}
