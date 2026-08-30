package vn.io.oldmoon.shopizer.user.business.event.rolemapping;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEventParser;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminRoleMappingEventListener
    implements ApplicationEventListener<KeycloakAdminEvent> {

  private static final Set<String> TRIGGER_ROLES =
      Set.of(
          Role.STORE_MANAGER.name(),
          Role.SUPER_ADMIN.name(),
          Role.SUPPORT_AGENT.name(),
          Role.WAREHOUSE_STAFF.name());

  private final UserService userService;
  private final EmployeeProfileService employeeProfileService;
  private final KeycloakAdminEventParser parser;

  @Override
  @RabbitListener(queues = RabbitMqConfig.adminRoleMappingCreatedQueue)
  public void handle(KeycloakAdminEvent event) {
    log.info(
        "Processing KeycloakAdminRoleMappingEvent: resourcePath={}, operationType={}, resourceType={}",
        event.resourcePath(),
        event.operationType(),
        event.resourceType());

    List<KeycloakRoleRepresentation> roles = parser.parseListRepresentations(event, KeycloakRoleRepresentation.class);
    if (!hasTriggerRole(roles)) {
      log.info(
          "No trigger operational roles found in event representation for resourcePath: {}. Skipping employee profile creation.",
          event.resourcePath());
      return;
    }

    UUID userId = parser.extractUserId(event);

    if (employeeProfileService.get(userId).isPresent()) {
      log.info("Employee profile already exists for userId: {}. Skipping creation.", userId);
      return;
    }

    User user = userService.get(userId);
    EmployeeProfile employeeProfile = toEmployeeProfileEntity(event, user);
    EmployeeProfile savedProfile = employeeProfileService.create(employeeProfile);
    log.info(
        "Successfully created employee profile: profileId={}, userId={}",
        savedProfile.getId(),
        userId);
  }

  public boolean hasTriggerRole(List<KeycloakRoleRepresentation> roles) {
    if (roles == null || roles.isEmpty()) {
      return false;
    }
    return roles.stream()
        .map(KeycloakRoleRepresentation::name)
        .filter(Objects::nonNull)
        .map(String::trim)
        .anyMatch(TRIGGER_ROLES::contains);
  }

  public EmployeeProfile toEmployeeProfileEntity(KeycloakAdminEvent event, User user) {
    EmployeeProfile employeeProfile = EmployeeProfile.builder().user(user).build();
    if (event.authDetails() != null && event.authDetails().userId() != null) {
      try {
        employeeProfile.setCreatedBy(UUID.fromString(event.authDetails().userId()));
      } catch (IllegalArgumentException e) {
        log.warn("Could not parse createdBy UUID from authDetails: {}", event.authDetails().userId());
      }
    }
    log.info(
        "Mapped KeycloakAdminRoleMappingEvent to EmployeeProfile entity: keycloakUserId={}",
        user.getKeycloakUserId());
    return employeeProfile;
  }
}
