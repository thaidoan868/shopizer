package vn.io.oldmoon.shopizer.user.business.event.email;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUpdateEmailEventListener
    implements ApplicationEventListener<KeycloakUpdateEmailEvent> {

  private final UserService userService;

  @Override
  @RabbitListener(queues = RabbitMqConfig.updateEmailQueue)
  public void handle(KeycloakUpdateEmailEvent event) {
    log.info("Processing KeycloakUpdateEmailEvent for userId={}", event.userId());
    Objects.requireNonNull(event);
    if (event.details() == null
        || event.details().updatedEmail() == null
        || event.details().updatedEmail().isBlank()
        || event.details().previousEmail() == null
        || event.details().previousEmail().isBlank()) {
      throw new InvalidInputException(
          "Update email event details or updated_email must not be null");
    }

    String updatedEmail = event.details().updatedEmail().trim();
    String previousEmail = event.details().previousEmail().trim();
    User user = userService.get(event.userId());
    if (!user.getEmail().equals(previousEmail)) {
      log.warn(
          "User email mismatch for keycloakUserId={}. Existing email: {}, Previous email: {}",
          user.getKeycloakUserId(),
          user.getEmail(),
          previousEmail);
    }
    user.setEmail(updatedEmail);
    user.setVerified(Boolean.FALSE);
    userService.update(user);
    log.info(
        "changed email from {} to {}",
        maskEmail(event.details().previousEmail()),
        maskEmail(updatedEmail));
  }

  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }
    int atIndex = email.indexOf('@');
    String localPart = email.substring(0, atIndex);
    String domainPart = email.substring(atIndex);
    if (localPart.length() <= 3) {
      return localPart.charAt(0) + "***" + domainPart;
    }
    return localPart.substring(0, 3) + "***" + domainPart;
  }
}
