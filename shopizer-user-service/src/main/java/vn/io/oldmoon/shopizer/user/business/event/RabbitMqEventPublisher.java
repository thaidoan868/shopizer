package vn.io.oldmoon.shopizer.user.business.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.io.oldmoon.shopizer.common.core.event.EventPublisher;
import vn.io.oldmoon.shopizer.common.core.event.RabbitMqEvent;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisher implements EventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void publish(RabbitMqEvent rabbitMqEvent) {
    if (rabbitMqEvent == null || rabbitMqEvent.event() == null) {
      log.warn("Attempted to publish a null event. Skipping.");
      return;
    }

    Object event = rabbitMqEvent.event();
    String routingKey = rabbitMqEvent.routingKey();

    try {
      rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);
      log.info(
          "Published event [{}] to exchange [{}] with routingKey [{}]",
          event.getClass().getSimpleName(),
          RabbitMqConfig.userEventExchange,
          routingKey);
    } catch (Exception ex) {
      log.error(
          "CRITICAL: Failed to publish event [{}] after commit. Event may be lost!",
          event.getClass().getSimpleName(),
          ex);
    }
  }
}

