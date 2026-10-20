package vn.io.oldmoon.shopizer.product.business.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.event.EventPublisher;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisher implements EventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(Object event, String routingKey) {
    if (event == null) {
      log.warn("Attempted to publish a null event. Skipping.");
      return;
    }

    rabbitTemplate.convertAndSend(RabbitMqConfig.productEventExchange, routingKey, event);
    log.info(
        "Published event [{}] to exchange [{}] with routingKey [{}]",
        event.getClass().getSimpleName(),
        RabbitMqConfig.productEventExchange,
        routingKey);
  }
}
