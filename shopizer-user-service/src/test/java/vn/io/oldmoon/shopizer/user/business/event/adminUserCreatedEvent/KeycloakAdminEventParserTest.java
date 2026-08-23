package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.infra.model.User;

class KeycloakAdminEventParserTest {

  private final KeycloakAdminEventParser parser = new KeycloakAdminEventParser(new ObjectMapper());

  @Nested
  @DisplayName("Resource Path User ID Extraction Tests")
  class ExtractUserIdTests {

    @Test
    @DisplayName("Should extract UUID from standard 'users/{uuid}' resourcePath")
    void extractUserId_WithStandardPath_ShouldReturnUuid() {
      UUID expected = UUID.fromString("a9cef68e-4cb9-4d69-bc3f-8f555c583ba5");
      UUID actual = parser.extractUserId("users/a9cef68e-4cb9-4d69-bc3f-8f555c583ba5");

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should extract UUID when resourcePath is plain UUID string")
    void extractUserId_WithPlainUuid_ShouldReturnUuid() {
      UUID expected = UUID.randomUUID();
      UUID actual = parser.extractUserId(expected.toString());

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should extract UUID from deeply nested path")
    void extractUserId_WithDeepPath_ShouldReturnUuid() {
      UUID expected = UUID.randomUUID();
      UUID actual = parser.extractUserId("admin/realms/shopizer/users/" + expected);

      assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Should throw InvalidInputException when resourcePath is null or blank")
    void extractUserId_WithNullOrBlankPath_ShouldThrowException(String path) {
      assertThatThrownBy(() -> parser.extractUserId(path))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("resourcePath must not be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"users/not-a-valid-uuid", "invalid-uuid-12345"})
    @DisplayName("Should throw InvalidInputException when resourcePath contains invalid UUID")
    void extractUserId_WithInvalidUuid_ShouldThrowException(String path) {
      assertThatThrownBy(() -> parser.extractUserId(path))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Invalid UUID in resourcePath");
    }
  }

  @Nested
  @DisplayName("Representation String Deserialization Tests")
  class ParseRepresentationTests {

    @Test
    @DisplayName("Should deserialize representation string correctly")
    void parseRepresentation_WithValidJson_ShouldPopulateFields() {

      String repJson =
          "{\\\"username\\\":\\\"napolenon\\\",\\\"firstName\\\":\\\"\\\",\\\"lastName\\\":\\\"\\\",\\\"email\\\":\\\"napoleon@france\\\",\\\"emailVerified\\\":false,\\\"attributes\\\":{\\\"locale\\\":[\\\"\\\"]},\\\"enabled\\\":true,\\\"requiredActions\\\":[],\\\"groups\\\":[]}";
      KeycloakAdminUserRepresentation rep = parser.parseRepresentation(repJson);

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
      assertThatThrownBy(() -> parser.parseRepresentation(json))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Representation JSON string must not be null or blank");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when representation JSON is malformed")
    void parseRepresentation_WithMalformedJson_ShouldThrowException() {
      assertThatThrownBy(() -> parser.parseRepresentation("{invalid_json: true"))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Malformed representation JSON");
    }
  }

  @Nested
  @DisplayName("User Entity Mapping Tests")
  class ToUserEntityTests {
    KeycloakAdminUserCreatedEvent event;
    UUID userId;

    @BeforeEach
    void setUp() {
      this.userId = UUID.fromString("a9cef68e-4cb9-4d69-bc3f-8f555c583ba5");

      this.event =
          KeycloakAdminUserCreatedEvent.builder()
              .resourcePath("users/" + userId)
              .authDetails(KeycloakAdminAuthDetails.builder().realmName("master").build())
              .representation(
                  """
                  {
                    "username": "napolenon",
                    "firstName": "",
                    "lastName": "",
                    "email": "napoleon@france",
                    "emailVerified": false
                  }
                  """)
              .build();
    }

    @Test
    @DisplayName("Should map event into User entity correctly")
    void toUserEntity_WithSampleEvent_ShouldCreateValidUserEntity() {
      User user = parser.toUserEntity(event);

      assertThat(user).isNotNull();
      assertThat(user.getRealm()).isEqualTo("master");
      assertThat(user.getKeycloakUserId()).isEqualTo(userId);
      assertThat(user.getUsername()).isEqualTo("napolenon");
      assertThat(user.getEmail()).isEqualTo("napoleon@france");
      assertThat(user.getFirstName()).isEmpty();
      assertThat(user.getLastName()).isEmpty();
      assertThat(user.getVerified()).isFalse();
    }

    @Test
    @DisplayName("Should throw InvalidInputException when event is null")
    void toUserEntity_WithNullEvent_ShouldThrowException() {
      assertThatThrownBy(() -> parser.toUserEntity(null))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("KeycloakAdminUserCreatedEvent must not be null");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when representation is null")
    void toUserEntity_WithNullRepresentation_ShouldThrowException() {
      UUID userId = UUID.randomUUID();
      assertThatThrownBy(() -> parser.toUserEntity(null, userId, event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("KeycloakAdminUserRepresentation must not be null");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when userId is null")
    void toUserEntity_WithNullUserId_ShouldThrowException() {
      KeycloakAdminUserRepresentation rep =
          KeycloakAdminUserRepresentation.builder()
              .username("testuser")
              .email("test@example.com")
              .build();

      assertThatThrownBy(() -> parser.toUserEntity(rep, null, event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("User ID must not be null");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when username is missing")
    void toUserEntity_WithMissingUsername_ShouldThrowException() {
      UUID userId = UUID.randomUUID();
      KeycloakAdminUserRepresentation rep =
          KeycloakAdminUserRepresentation.builder().email("test@example.com").username("").build();

      assertThatThrownBy(() -> parser.toUserEntity(rep, userId, event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Username must not be null or blank");
    }

    @Test
    @DisplayName("Should throw InvalidInputException when email is missing")
    void toUserEntity_WithMissingEmail_ShouldThrowException() {
      UUID userId = UUID.randomUUID();
      KeycloakAdminUserRepresentation rep =
          KeycloakAdminUserRepresentation.builder().username("testuser").email(null).build();

      assertThatThrownBy(() -> parser.toUserEntity(rep, userId, event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Email must not be null or blank");
    }
  }
}
