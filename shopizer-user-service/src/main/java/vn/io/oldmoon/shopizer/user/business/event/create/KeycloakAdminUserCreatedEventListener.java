package vn.io.oldmoon.shopizer.user.business.event.create;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.event.admin.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.event.admin.KeycloakAdminEventParser;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminUserCreatedEventListener
    implements ApplicationEventListener<KeycloakAdminEvent> {

  private final EmployeeProfileService employeeProfileService;
  private final KeycloakAdminEventParser parser;

  @Override
  @RabbitListener(queues = RabbitMqConfig.AdminUserCreatedQueue)
  public void handle(KeycloakAdminEvent event) {
    log.info(
        "Processing KeycloakAdminUserCreatedEvent: resourcePath={}, operationType={}, resourceType={}",
        event.resourcePath(),
        event.operationType(),
        event.resourceType());

    User user = toUserEntity(event);
    employeeProfileService.create(user);
    log.info(
        "Successfully synced user from admin create event: keycloakUserId={}",
        user.getCreatedBy() != null ? user.getCreatedBy() : null);
  }

  /**
   * Maps a KeycloakAdminEvent to a User entity.
   *
   * @throws InvalidInputException The user must contain keycloakUserId, username, and email. If any
   *     of these fields are missing or blank, an exception is thrown.
   */
  public User toUserEntity(KeycloakAdminEvent event) {
    Objects.requireNonNull(event);

    UUID userId = parser.extractUserId(event);

    KeycloakAdminUserCreatedRepresentation representation =
        parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class);

    if (representation.username() == null || representation.username().isBlank()) {
      throw new InvalidInputException("Username must not be null or blank");
    }
    if (representation.email() == null || representation.email().isBlank()) {
      throw new InvalidInputException("Email must not be null or blank");
    }

    User user =
        User.builder()
            .keycloakUserId(userId)
            .username(representation.username().trim())
            .email(representation.email().trim())
            .firstName(representation.firstName())
            .lastName(representation.lastName())
            .verified(representation.emailVerified())
            .build();
    log.info(
        "Mapped KeycloakAdminUserCreatedEvent to User entity: keycloakUserId={}, username={}, email={}",
        user.getKeycloakUserId(),
        user.getUsername(),
        user.getEmail());
    if (event.authDetails() != null && event.authDetails().userId() != null) {
      try {
        UUID createdBy = UUID.fromString(event.authDetails().userId());
        user.setCreatedBy(createdBy);
      } catch (Exception e) {
        log.warn(
            "Could not parse createdBy UUID from authDetails: {}", event.authDetails().userId());
      }
    }
    return user;
  }
}
