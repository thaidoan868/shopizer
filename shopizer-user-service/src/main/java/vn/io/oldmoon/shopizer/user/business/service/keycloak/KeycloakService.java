package vn.io.oldmoon.shopizer.user.business.service.keycloak;

import jakarta.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.exception.*;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {
  private final Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String realm;

  /**
   * @throws NotFoundException if not found user
   */
  public UserRepresentation get(UUID userId) {
    String id = userId.toString();
    UsersResource users = keycloak.realm(realm).users();
    UserResource user = users.get(id);
    UserRepresentation userRep;
    userRep = user.toRepresentation();
    return userRep;
  }

  /**
   * @throws ResourceNotFoundException if user not found
   * @throws InvalidInputException if username is blank
   */
  public UserRepresentation getUserByUsername(String username) {
    if (username.isBlank()) {
      throw new InvalidInputException("Username must not be blank");
    }

    List<UserRepresentation> users =
        keycloak.realm(realm).users().searchByUsername(username.trim(), true);

    if (users.isEmpty()) {
      throw new ResourceNotFoundException("KeycloakUser", "username=" + username);
    }

    if (users.size() > 1) {
      log.warn("Multiple users found with username={}", username);
    }

    return users.getFirst();
  }

  public void assignRealmRole(String userId, Role role) {
    Objects.requireNonNull(userId);
    Objects.requireNonNull(role);
    // Get user
    RealmResource realmResource = keycloak.realm(realm);
    UserResource userResource = realmResource.users().get(userId);

    // Get role
    RoleRepresentation roleRepresentation;
    try {
      roleRepresentation = realmResource.roles().get(role.name()).toRepresentation();
    } catch (NotFoundException e) {
      throw new ResourceNotFoundException("Role", "roleName=" + role.name());
    }

    // Assign role
    userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
    log.info("Role assigned to user: userId={}, roleName={}", userId, role.name());
  }
}
