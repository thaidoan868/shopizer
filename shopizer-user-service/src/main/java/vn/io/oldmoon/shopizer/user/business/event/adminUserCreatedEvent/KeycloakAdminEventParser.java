package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminEventParser {

  private final ObjectMapper objectMapper;

  public UUID extractUserId(String resourcePath) {
    if (resourcePath == null || resourcePath.isBlank()) {
      throw new InvalidInputException("resourcePath must not be null or blank");
    }
    String path = resourcePath.trim();
    if (path.contains("/")) {
      path = path.substring(path.lastIndexOf('/') + 1);
    }
    try {
      return UUID.fromString(path);
    } catch (IllegalArgumentException e) {
      log.error("Failed to parse UUID from resourcePath: {}", resourcePath, e);
      throw new InvalidInputException("Invalid UUID in resourcePath: " + resourcePath);
    }
  }

  public KeycloakAdminUserRepresentation parseRepresentation(String representationJson) {
    if (representationJson == null || representationJson.isBlank()) {
      throw new InvalidInputException("Representation JSON string must not be null or blank");
    }
    try {
      String cleanJson = representationJson.replace("\\\"", "\"");
      return objectMapper.readValue(cleanJson, KeycloakAdminUserRepresentation.class);
    } catch (JsonProcessingException e) {
      throw new InvalidInputException("Malformed representation JSON: " + e.getMessage());
    }
  }

  public User toUserEntity(KeycloakAdminUserCreatedEvent event) {
    if (event == null) {
      throw new InvalidInputException("KeycloakAdminUserCreatedEvent must not be null");
    }

    UUID userId = event.getExtractedUserId();
    if (userId == null) {
      throw new InvalidInputException(
          "Cannot extract valid user UUID from resourcePath: " + event.resourcePath());
    }

    KeycloakAdminUserRepresentation representation = parseRepresentation(event.representation());

    return toUserEntity(representation, userId, event);
  }

  public User toUserEntity(
      KeycloakAdminUserRepresentation representation,
      UUID userId,
      KeycloakAdminUserCreatedEvent event) {
    if (representation == null) {
      throw new InvalidInputException("KeycloakAdminUserRepresentation must not be null");
    }

    if (userId == null) {
      throw new InvalidInputException("User ID must not be null");
    }

    if (representation.username() == null || representation.username().isBlank()) {
      throw new InvalidInputException("Username must not be null or blank");
    }
    if (representation.email() == null || representation.email().isBlank()) {
      throw new InvalidInputException("Email must not be null or blank");
    }

    return User.builder()
        .realm(event.authDetails().realmName().isBlank() ? null : event.authDetails().realmName())
        .keycloakUserId(userId)
        .username(representation.username().trim())
        .email(representation.email().trim())
        .firstName(representation.firstName())
        .lastName(representation.lastName())
        .verified(representation.emailVerified())
        .build();
  }
}
