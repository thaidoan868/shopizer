package vn.io.oldmoon.shopizer.user.business.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.event.EventPublisher;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.event.UserCreatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreatedPublisher implements EventPublisher<UserCreatedEvent> {
  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(UserCreatedEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.userRegisteredBindingKey, event);

    log.info(
        "Published an event: userId={}, bindingKey={}",
        event.userId(),
        RabbitMqConfig.userRegisteredBindingKey);
  }
}
