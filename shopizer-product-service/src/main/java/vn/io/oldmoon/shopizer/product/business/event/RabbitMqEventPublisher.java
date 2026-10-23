package vn.io.oldmoon.shopizer.product.business.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.io.oldmoon.shopizer.common.core.event.EventPublisher;
import vn.io.oldmoon.shopizer.common.core.event.RabbitMqEvent;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisher implements EventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(RabbitMqEvent rabbitMqEvent) {
    if (rabbitMqEvent == null || rabbitMqEvent.event() == null) {
      log.warn("Attempted to publish a null event. Skipping.");
      return;
    }

    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              send(rabbitMqEvent);
            }
          });
    } else {
      // Immediate fallback if called outside an active transactionn
      send(rabbitMqEvent);
    }
  }

  private void send(RabbitMqEvent rabbitMqEvent) {
    Object event = rabbitMqEvent.event();
    String routingKey = rabbitMqEvent.routingKey();

    try {
      rabbitTemplate.convertAndSend(RabbitMqConfig.productEventExchange, routingKey, event);
      log.info(
          "Published event [{}] to exchange [{}] with routingKey [{}] AFTER COMMIT",
          event.getClass().getSimpleName(),
          RabbitMqConfig.productEventExchange,
          routingKey);
    } catch (Exception ex) {
      log.error(
          "CRITICAL: Failed to publish event [{}] after commit. Event may be lost!",
          event.getClass().getSimpleName(),
          ex);
    }
  }
}
