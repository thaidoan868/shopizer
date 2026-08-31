package vn.io.oldmoon.shopizer.user.business.service.keycloak;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
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

  /**
   * @throws ApiException if the user already exists (409)
   * @throws ServiceException if there is a general failure in the identity provider logic
   * @throws WebApplicationException if keycloak client failed to authenticate or missed required
   *     roles
   * @throws ProcessingException if a network timeout or connectivity issue occurs
   */
  @Deprecated
  public String create(UserRepresentation registerUser) {
    UsersResource usersResource = keycloak.realm(realm).users();

    // creation
    String userId;
    try (Response userResponse = usersResource.create(registerUser)) {
      int status = userResponse.getStatus();
      String body = null;

      // failure
      if (status != 201 && userResponse.hasEntity()) {
        body = userResponse.readEntity(String.class);
      }

      if (status == 409) {
        log.info(
            "Failed to create a new account (conflict). username={}, email={}, kcBody={}",
            registerUser.getUsername(),
            registerUser.getEmail(),
            body);

        KeycloakErrorResponse errorResponse =
            userResponse.readEntity(KeycloakErrorResponse.class); // parse safely
        throw new ApiException(ErrorCode.CONFLICT, errorResponse.getErrorMessage());
      }

      if (status != 201) {
        String locationPath =
            userResponse.getLocation() != null ? userResponse.getLocation().getPath() : null;

        log.error(
            "Keycloak failed to create a new account. status={}, username={}, email={}, locationPath={}, kcBody={}",
            status,
            registerUser.getUsername(),
            registerUser.getEmail(),
            locationPath,
            body);

        throw new ServiceException(
            "Failed to create a new account: locationPath=%s, body=%s, status=%s"
                .formatted(locationPath, body, status));
      }

      // successful
      userId = CreatedResponseUtil.getCreatedId(userResponse);
      log.info("Keycloak created a user with id={}", userId);
    } catch (Exception e) {
      // I used try catch command to ensure userResponse is closed automatically
      throw e;
    }

    return userId;
  }

  /**
   * @throws RuntimeException if auth or network errors occur
   */
  @Deprecated
  public void resetPassword(String userId, String newPassword) {
    UsersResource usersResource = keycloak.realm(realm).users();
    UserResource userResource = usersResource.get(userId);

    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue(newPassword);
    passwordCred.setTemporary(false);

    userResource.resetPassword(passwordCred);

    userResource.logout();

    log.info("Password reset and sessions invalidated for userId={}", userId);
  }

  /**
   * @throws RuntimeException if auth or network errors occur
   */
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
      log.error("Not found role with name '{}'", role.name());
      throw e;
    }

    // Assign role
    userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));

    log.info("Role assigned to user: userId={}, roleName={}", userId, role.name());
  }

  /**
   * @throws RuntimeException if auth or network errors occur
   * @throws IllegalArgumentException if user is blank
   */
  public void update(UserRepresentation userRepresentation) {
    Objects.requireNonNull(userRepresentation, "User representation must not be null");
    // get user
    if (userRepresentation.getId() == null | userRepresentation.getId().isBlank()) {
      throw new IllegalArgumentException("User id must not be null or blank");
    }
    UserResource userResource;
    try {
      userResource = keycloak.realm(realm).users().get(userRepresentation.getId());
    } catch (NotFoundException ex) {
      throw new RuntimeException("User not found: " + userRepresentation.getId());
    }

    // update user
    userResource.update(userRepresentation);

    log.info("User updated: id={}", userRepresentation.getId());
  }
}
