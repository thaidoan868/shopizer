package vn.io.oldmoon.shopizer.user.business.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.EventListener;
import vn.io.oldmoon.shopizer.common.event.UserCreatedEvent;
import vn.io.oldmoon.shopizer.rabbitmq.RabbitConstants;

@Component
@RequiredArgsConstructor
public class UserCreatedListener implements EventListener<UserCreatedEvent> {

  @Override
  @RabbitListener(queues = RabbitConstants.USER_CREATED_QUEUE)
  public void handle(UserCreatedEvent message) {
    System.out.println("Received message: " + message);
    throw new RuntimeException("Failed intentionally");
  }
}
