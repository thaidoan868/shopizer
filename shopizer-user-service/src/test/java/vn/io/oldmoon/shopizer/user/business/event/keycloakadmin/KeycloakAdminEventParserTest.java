package vn.io.oldmoon.shopizer.user.business.event.keycloakadmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.business.event.create.KeycloakAdminUserCreatedRepresentation;

class KeycloakAdminEventParserTest {

  private final KeycloakAdminEventParser parser = new KeycloakAdminEventParser(new ObjectMapper());

  @Nested
  @DisplayName("Resource Path User ID Extraction Tests")
  class ExtractUserIdTests {

    @Test
    @DisplayName("Should extract UUID from standard 'users/{uuid}' resourcePath")
    void extractUserId_WithStandardPath_ShouldReturnUuid() {
      UUID expected = UUID.fromString("a9cef68e-4cb9-4d69-bc3f-8f555c583ba5");
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder().resourcePath("users/" + expected).build();

      UUID actual = parser.extractUserId(event);

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should extract UUID from 'users/{uuid}/abc/pdf' resourcePath")
    void extractUserId_WithSubPath_ShouldReturnUuid() {
      UUID expected = UUID.fromString("a9cef68e-4cb9-4d69-bc3f-8f555c583ba5");
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder().resourcePath("users/" + expected + "/abc/pdf").build();

      UUID actual = parser.extractUserId(event);

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should extract UUID from deeply nested path")
    void extractUserId_WithDeepPath_ShouldReturnUuid() {
      UUID expected = UUID.randomUUID();
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder()
              .resourcePath("admin/realms/shopizer/users/" + expected)
              .build();

      UUID actual = parser.extractUserId(event);

      assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw InvalidInputException when resourcePath is null or blank")
    void extractUserId_WithNullOrBlankPath_ShouldThrowException(String path) {
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().resourcePath(path).build();

      assertThatThrownBy(() -> parser.extractUserId(event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("resourcePath must not be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"users/not-a-valid-uuid", "users/invalid-uuid-12345"})
    @DisplayName("Should throw InvalidInputException when resourcePath contains invalid UUID")
    void extractUserId_WithInvalidUuid_ShouldThrowException(String path) {
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().resourcePath(path).build();

      assertThatThrownBy(() -> parser.extractUserId(event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Invalid UUID in resourcePath");
    }

    @Test
    @DisplayName(
        "Should throw InvalidInputException when resourcePath does not contain users/ prefix")
    void extractUserId_WithoutUsersPrefix_ShouldThrowException() {
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder().resourcePath("realm/shopizer/settings").build();

      assertThatThrownBy(() -> parser.extractUserId(event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("resourcePath does not contain a valid users/{userId} structure");
    }
  }

  @Nested
  @DisplayName("Representation String Deserialization Tests")
  class ParseRepresentationTests {

    @Test
    @DisplayName("Should deserialize representation string correctly")
    void parseRepresentation_WithValidJson_ShouldPopulateFields() {
      String repJson =
          "{\"username\":\"napolenon\",\"firstName\":\"\",\"lastName\":\"\",\"email\":\"napoleon@france\",\"emailVerified\":false,\"attributes\":{\"locale\":[\"\"]},\"enabled\":true,\"requiredActions\":[],\"groups\":[]}";
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().representation(repJson).build();

      KeycloakAdminUserCreatedRepresentation rep =
          parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class);

      assertThat(rep).isNotNull();
      assertThat(rep.username()).isEqualTo("napolenon");
      assertThat(rep.email()).isEqualTo("napoleon@france");
      assertThat(rep.emailVerified()).isFalse();
      assertThat(rep.enabled()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw InvalidInputException when representation JSON is null or blank")
    void parseRepresentation_WithNullOrBlankJson_ShouldThrowException(String json) {
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().representation(json).build();

      assertThatThrownBy(
              () -> parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Representation JSON string must not be null or blank");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when representation JSON is malformed")
    void parseRepresentation_WithMalformedJson_ShouldThrowException() {
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder().representation("{invalid_json: true").build();

      assertThatThrownBy(
              () -> parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Malformed representation JSON");
    }
  }
}
