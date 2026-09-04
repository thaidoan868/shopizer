package vn.io.oldmoon.shopizer.user.business.event.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.service.UserService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakVerifyEmailEventListener
    implements ApplicationEventListener<KeycloakVerifyEmailEvent> {

  private final UserService userService;

  @Override
  @RabbitListener(queues = RabbitMqConfig.verifyEmailQueue)
  public void handle(KeycloakVerifyEmailEvent event) {
    if (event == null || event.userId() == null) {
      throw new InvalidInputException("Verify email event or userId must not be null");
    }
    log.info("Processing KeycloakVerifyEmailEvent for userId={}", event.userId());
    if (event.details() == null
        || event.details().email() == null
        || event.details().email().isBlank()) {
      throw new InvalidInputException("Verify email event details or email must not be null");
    }

    userService.verifyEmail(event.userId(), event.details().email());
    log.info("Successfully updated email verification status for userId={}", event.userId());
  }
}
