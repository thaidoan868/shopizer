package vn.io.oldmoon.shopizer.user.business.event.email;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceConflictException;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakVerifyEmailEventListener
    implements ApplicationEventListener<KeycloakVerifyEmailEvent> {

  private final UserService userService;
  private final UserRepository userRepository;

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

    UUID keycloakUserId = event.userId();
    String verifiedEmail = event.details().email().trim();
    User user = userService.get(keycloakUserId);
    if (!user.getEmail().equals(verifiedEmail)) {
      log.warn(
          "User email mismatch for keycloakUserId={}. Existing email: {}, Verified email: {}",
          keycloakUserId,
          user.getEmail(),
          verifiedEmail);
      throw new ResourceConflictException(
          "Verified email does not match the existing email for user");
    }

    user.setVerified(Boolean.TRUE);
    userRepository.save(user);
    log.info("Successfully updated email verification status for userId={}", event.userId());
  }
}
