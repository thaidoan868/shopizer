package vn.io.oldmoon.shopizer.user.business.event.listener;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.EventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.event.UserCreatedEvent;
import vn.io.oldmoon.shopizer.user.business.service.CustomerService;
import vn.io.oldmoon.shopizer.user.infra.model.User;

// create a profile for the new user
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedCreateProfileListener implements EventListener<UserCreatedEvent> {
  private final CustomerService customerService;

  @Override
  @RabbitListener(queues = RabbitMqConfig.userCreatedQueue)
  public void handle(UserCreatedEvent event) {
    log.info(
        "Processing user created event: Creating customer profile for userId={}", event.userId());
    User profile =
        User.builder()
            .userId(UUID.fromString(event.userId()))
            .email(event.email())
            .username(event.username())
            .firstName(event.firstName())
            .lastName(event.lastName())
            .build();
    User savedProfile = customerService.createProfile(profile);
    log.info("Created customer profile: id={},  userId={}", profile.getId(), profile.getUserId());
  }
}
