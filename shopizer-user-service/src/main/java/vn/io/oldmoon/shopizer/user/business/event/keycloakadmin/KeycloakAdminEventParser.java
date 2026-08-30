package vn.io.oldmoon.shopizer.user.business.event.keycloakadmin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminEventParser {

  private final Pattern USER_ID_PATTERN = Pattern.compile("users/([^/]+)");
  private final ObjectMapper objectMapper;

  /**
   * Extracts the user ID from a given resource path. The expected format is
   * "users/{userId}/abc/pdf".
   *
   * @return the extracted UUID of the user
   * @throws InvalidInputException if the resourcePath is null, blank, or does not contain a valid
   *     user ID
   */
  public UUID extractUserId(KeycloakAdminEvent event) {
    String resourcePath = event.resourcePath();

    if (resourcePath == null || resourcePath.isBlank()) {
      throw new InvalidInputException("resourcePath must not be null or blank");
    }

    Matcher matcher = USER_ID_PATTERN.matcher(resourcePath.trim());
    if (matcher.find()) {
      String rawUserId = matcher.group(1);
      try {
        return UUID.fromString(rawUserId);
      } catch (IllegalArgumentException e) {
        log.error(
            "Failed to parse UUID from extracted user segment: {} in resourcePath: {}",
            rawUserId,
            resourcePath,
            e);
        throw new InvalidInputException("Invalid UUID in resourcePath: " + resourcePath);
      }
    }

    throw new InvalidInputException(
        "resourcePath does not contain a valid users/{userId} structure: " + resourcePath);
  }

  /**
   * Parses the representation JSON string into an object of type T.
   *
   * @param <T> the target object type
   * @param clazz the target class type
   * @return an instance of type T representing the parsed JSON
   * @throws InvalidInputException if representation is null, blank, or malformed
   */
  public <T> T parseRepresentation(KeycloakAdminEvent event, Class<T> clazz) {
    String representation = event.representation();
    if (representation == null || representation.isBlank()) {
      throw new InvalidInputException("Representation JSON string must not be null or blank");
    }
    try {
      return objectMapper.readValue(representation, clazz);
    } catch (JsonProcessingException e) {
      throw new InvalidInputException("Malformed representation JSON: " + e.getMessage());
    }
  }
}
