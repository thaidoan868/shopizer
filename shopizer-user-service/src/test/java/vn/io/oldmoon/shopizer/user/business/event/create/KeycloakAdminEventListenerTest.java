package vn.io.oldmoon.shopizer.user.business.event.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminAuthDetails;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEventParser;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminEventListenerTest {

  @Mock private KeycloakAdminEventParser parser;

  @Mock private EmployeeProfileService employeeProfileService;

  @InjectMocks private KeycloakAdminUserCreatedEventListener listener;

  @Nested
  @DisplayName("handle method tests")
  class HandleTests {

    @Test
    @DisplayName("handle should parse event and delegate to EmployeeProfileService.create(User)")
    void handle_ShouldParseAndPersistUserAndEmployeeProfile() {
      // Given
      UUID userId = UUID.randomUUID();
      UUID creatorId = UUID.randomUUID();
      KeycloakAdminAuthDetails authDetails =
          KeycloakAdminAuthDetails.builder().userId(creatorId.toString()).build();
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder()
              .resourcePath("users/" + userId)
              .authDetails(authDetails)
              .build();

      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username("napoleon")
              .email("napoleon@france")
              .firstName("Napoleon")
              .lastName("Bonaparte")
              .emailVerified(true)
              .build();

      when(parser.extractUserId(event)).thenReturn(userId);
      when(parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);
      when(employeeProfileService.create(any(User.class)))
          .thenReturn(EmployeeProfile.builder().build());

      // When
      listener.handle(event);

      // Then
      verify(parser).extractUserId(event);
      verify(parser).parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class);
      verify(employeeProfileService)
          .create(
              (User)
                  argThat(
                      u ->
                          u instanceof User user
                              && user.getKeycloakUserId().equals(userId)
                              && "napoleon".equals(user.getUsername())
                              && "napoleon@france".equals(user.getEmail())
                              && "Napoleon".equals(user.getFirstName())
                              && "Bonaparte".equals(user.getLastName())
                              && Boolean.TRUE.equals(user.getVerified())
                              && creatorId.equals(user.getCreatedBy())));
    }
  }

  @Nested
  @DisplayName("toUserEntity mapping tests")
  class ToUserEntityTests {

    private UUID userId;
    private UUID creatorId;
    private KeycloakAdminEvent event;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      creatorId = UUID.randomUUID();
      KeycloakAdminAuthDetails authDetails =
          KeycloakAdminAuthDetails.builder().userId(creatorId.toString()).build();
      event =
          KeycloakAdminEvent.builder()
              .resourcePath("users/" + userId)
              .authDetails(authDetails)
              .build();
    }

    @Test
    @DisplayName("toUserEntity should map all representation and event fields correctly")
    void toUserEntity_WithValidEventAndAuthDetails_ShouldMapAllFields() {
      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username(" napoleon ")
              .email(" napoleon@france ")
              .firstName("Napoleon")
              .lastName("Bonaparte")
              .emailVerified(true)
              .build();

      when(parser.extractUserId(event)).thenReturn(userId);
      when(parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);

      User user = listener.toUserEntity(event);

      assertThat(user).isNotNull();
      assertThat(user.getKeycloakUserId()).isEqualTo(userId);
      assertThat(user.getUsername()).isEqualTo("napoleon");
      assertThat(user.getEmail()).isEqualTo("napoleon@france");
      assertThat(user.getFirstName()).isEqualTo("Napoleon");
      assertThat(user.getLastName()).isEqualTo("Bonaparte");
      assertThat(user.getVerified()).isTrue();
      assertThat(user.getCreatedBy()).isEqualTo(creatorId);
    }

    @Test
    @DisplayName("toUserEntity should throw NullPointerException when event is null")
    void toUserEntity_WithNullEvent_ShouldThrowNullPointerException() {
      assertThatThrownBy(() -> listener.toUserEntity(null))
          .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("toUserEntity should throw InvalidInputException when username is null or blank")
    void toUserEntity_WithNullOrBlankUsername_ShouldThrowInvalidInputException(String username) {
      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username(username)
              .email("napoleon@france")
              .build();

      when(parser.extractUserId(event)).thenReturn(userId);
      when(parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);

      assertThatThrownBy(() -> listener.toUserEntity(event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Username must not be null or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("toUserEntity should throw InvalidInputException when email is null or blank")
    void toUserEntity_WithNullOrBlankEmail_ShouldThrowInvalidInputException(String email) {
      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username("napoleon")
              .email(email)
              .build();

      when(parser.extractUserId(event)).thenReturn(userId);
      when(parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);

      assertThatThrownBy(() -> listener.toUserEntity(event))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Email must not be null or blank");
    }

    @Test
    @DisplayName("toUserEntity should handle invalid UUID in authDetails userId gracefully")
    void toUserEntity_WithInvalidAuthDetailsUserId_ShouldHandleGracefully() {
      KeycloakAdminAuthDetails invalidAuthDetails =
          KeycloakAdminAuthDetails.builder().userId("invalid-uuid").build();
      KeycloakAdminEvent eventWithInvalidAuth =
          KeycloakAdminEvent.builder()
              .resourcePath("users/" + userId)
              .authDetails(invalidAuthDetails)
              .build();

      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username("napoleon")
              .email("napoleon@france")
              .build();

      when(parser.extractUserId(eventWithInvalidAuth)).thenReturn(userId);
      when(parser.parseRepresentation(
              eventWithInvalidAuth, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);

      User user = listener.toUserEntity(eventWithInvalidAuth);

      assertThat(user).isNotNull();
      assertThat(user.getCreatedBy()).isNull();
    }

    @Test
    @DisplayName("toUserEntity should map fields without createdBy when authDetails is null")
    void toUserEntity_WithNullAuthDetails_ShouldMapWithoutCreatedBy() {
      KeycloakAdminEvent eventWithoutAuth =
          KeycloakAdminEvent.builder().resourcePath("users/" + userId).authDetails(null).build();

      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username("napoleon")
              .email("napoleon@france")
              .build();

      when(parser.extractUserId(eventWithoutAuth)).thenReturn(userId);
      when(parser.parseRepresentation(
              eventWithoutAuth, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);

      User user = listener.toUserEntity(eventWithoutAuth);

      assertThat(user).isNotNull();
      assertThat(user.getCreatedBy()).isNull();
    }
  }
}
