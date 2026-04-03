package vn.io.oldmoon.shopizer.rabbitmq.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.event.EventPublisher;
import vn.io.oldmoon.shopizer.common.event.UserCreatedEvent;
import vn.io.oldmoon.shopizer.rabbitmq.RabbitConstants;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreatedPublisher implements EventPublisher<UserCreatedEvent> {
  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(UserCreatedEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitConstants.MAIN_EXCHANGE, RabbitConstants.USER_CREATED_KEY, event);

    log.info(
        "Published an event to {}: userId={}", RabbitConstants.USER_CREATED_QUEUE, event.userId());
  }
}
