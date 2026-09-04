package vn.io.oldmoon.shopizer.user.business.event.admin;

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
import vn.io.oldmoon.shopizer.user.business.event.role.KeycloakRoleRepresentation;

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

  @Nested
  @DisplayName("List Representation Deserialization Tests")
  class ParseListRepresentationTests {

    @Test
    @DisplayName("Should parse JSON array of role representations correctly")
    void parseListRepresentations_WithJsonArray_ShouldReturnListOfRoles() {
      String json =
          """
          [
            {
              "id": "2b230619-bcfc-4ae4-b608-954a4b185290",
              "name": "SUPER_ADMIN",
              "description": "Full System Access",
              "composite": false,
              "clientRole": false,
              "containerId": "a9cbd686-1d46-44c5-9c61-26078d493828"
            },
            {
              "id": "3c341720-cdfd-5bf5-c719-065b5c296301",
              "name": "STORE_MANAGER",
              "description": "Store Manager Access",
              "composite": false,
              "clientRole": false,
              "containerId": "a9cbd686-1d46-44c5-9c61-26078d493828"
            }
          ]
          """;
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().representation(json).build();

      var roles = parser.parseListRepresentations(event, KeycloakRoleRepresentation.class);

      assertThat(roles).hasSize(2);
      assertThat(roles.get(0).name()).isEqualTo("SUPER_ADMIN");
      assertThat(roles.get(0).id()).isEqualTo("2b230619-bcfc-4ae4-b608-954a4b185290");
      assertThat(roles.get(0).description()).isEqualTo("Full System Access");
      assertThat(roles.get(1).name()).isEqualTo("STORE_MANAGER");
      assertThat(roles.get(1).id()).isEqualTo("3c341720-cdfd-5bf5-c719-065b5c296301");
    }

    @Test
    @DisplayName("Should parse single JSON object of role representation correctly")
    void parseListRepresentations_WithSingleObject_ShouldReturnSingletonList() {
      String json =
          """
          {
            "id": "2b230619-bcfc-4ae4-b608-954a4b185290",
            "name": "SUPPORT_AGENT",
            "description": "Customer Support",
            "composite": false,
            "clientRole": false,
            "containerId": "a9cbd686-1d46-44c5-9c61-26078d493828"
          }
          """;
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().representation(json).build();

      var roles = parser.parseListRepresentations(event, KeycloakRoleRepresentation.class);

      assertThat(roles).hasSize(1);
      assertThat(roles.get(0).name()).isEqualTo("SUPPORT_AGENT");
      assertThat(roles.get(0).id()).isEqualTo("2b230619-bcfc-4ae4-b608-954a4b185290");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw InvalidInputException when representation is null or blank")
    void parseListRepresentations_WithNullOrBlankJson_ShouldThrowException(String json) {
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().representation(json).build();

      assertThatThrownBy(
              () -> parser.parseListRepresentations(event, KeycloakRoleRepresentation.class))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Representation JSON string must not be null or blank");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when representation is malformed")
    void parseListRepresentations_WithMalformedJson_ShouldThrowException() {
      String json =
          """
        ["field1": "value1", "field2": "value2"]  // Does not represent JSON objects
        """;
      KeycloakAdminEvent event = KeycloakAdminEvent.builder().representation(json).build();

      assertThatThrownBy(
              () -> parser.parseListRepresentations(event, KeycloakRoleRepresentation.class))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Malformed representation JSON");
    }
  }
}
