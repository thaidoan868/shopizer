package vn.io.oldmoon.shopizer.user.business.event.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@ExtendWith(MockitoExtension.class)
class KeycloakUpdateEmailEventListenerTest {

  @Mock private UserService userService;

  @InjectMocks private KeycloakUpdateEmailEventListener listener;

  @Test
  @DisplayName("handle should update user email and reset verified to false when event is valid")
  void handle_WhenValidEvent_ShouldUpdateEmailAndResetVerified() {
    // Given
    UUID userId = UUID.randomUUID();
    String previousEmail = "old@example.com";
    String updatedEmail = "new@example.com";
    User user =
        User.builder()
            .keycloakUserId(userId)
            .username("testuser")
            .email(previousEmail)
            .verified(true)
            .build();

    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(updatedEmail)
            .previousEmail(previousEmail)
            .build();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(details)
            .build();

    when(userService.get(userId)).thenReturn(user);

    // When
    listener.handle(event);

    // Then
    verify(userService).get(userId);
    assertThat(user.getEmail()).isEqualTo(updatedEmail);
    assertThat(user.getVerified()).isFalse();
    verify(userService).update(user);
  }

  @Test
  @DisplayName(
      "handle should still update user email and log warning when previousEmail mismatches existing email")
  void handle_WhenPreviousEmailMismatches_ShouldStillUpdateEmail() {
    // Given
    UUID userId = UUID.randomUUID();
    String existingEmail = "existing@example.com";
    String previousEmail = "other_old@example.com";
    String updatedEmail = "new@example.com";
    User user =
        User.builder()
            .keycloakUserId(userId)
            .username("testuser")
            .email(existingEmail)
            .verified(true)
            .build();

    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(updatedEmail)
            .previousEmail(previousEmail)
            .build();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(details)
            .build();

    when(userService.get(userId)).thenReturn(user);

    // When
    listener.handle(event);

    // Then
    verify(userService).get(userId);
    assertThat(user.getEmail()).isEqualTo(updatedEmail);
    assertThat(user.getVerified()).isFalse();
    verify(userService).update(user);
  }

  @Test
  @DisplayName("handle should trim updatedEmail and previousEmail")
  void handle_WhenEmailsHaveWhitespace_ShouldTrimEmails() {
    // Given
    UUID userId = UUID.randomUUID();
    User user =
        User.builder()
            .keycloakUserId(userId)
            .username("testuser")
            .email("old@example.com")
            .verified(true)
            .build();

    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail("   new@example.com   ")
            .previousEmail("   old@example.com   ")
            .build();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(details)
            .build();

    when(userService.get(userId)).thenReturn(user);

    // When
    listener.handle(event);

    // Then
    assertThat(user.getEmail()).isEqualTo("new@example.com");
    verify(userService).update(user);
  }

  @Test
  @DisplayName("handle should throw NullPointerException when event is null")
  void handle_WhenEventNull_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> listener.handle(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("handle should throw InvalidInputException when event details is null")
  void handle_WhenDetailsNull_ShouldThrowInvalidInputException() {
    UUID userId = UUID.randomUUID();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(null)
            .build();

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("details or updated_email must not be null");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName(
      "handle should throw InvalidInputException when details updated_email is null or blank")
  void handle_WhenUpdatedEmailNullOrBlank_ShouldThrowInvalidInputException(String updatedEmail) {
    UUID userId = UUID.randomUUID();
    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(updatedEmail)
            .previousEmail("old@example.com")
            .build();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(details)
            .build();

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("details or updated_email must not be null");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName(
      "handle should throw InvalidInputException when details previous_email is null or blank")
  void handle_WhenPreviousEmailNullOrBlank_ShouldThrowInvalidInputException(String previousEmail) {
    UUID userId = UUID.randomUUID();
    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail("new@example.com")
            .previousEmail(previousEmail)
            .build();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(details)
            .build();

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("details or updated_email must not be null");
  }

  @Test
  @DisplayName("handle should mask email correctly for short local parts")
  void handle_WhenEmailsHaveShortLocalParts_ShouldNotFail() {
    UUID userId = UUID.randomUUID();
    User user =
        User.builder()
            .keycloakUserId(userId)
            .username("testuser")
            .email("ab@example.com")
            .verified(true)
            .build();

    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail("cd@example.com")
            .previousEmail("ab@example.com")
            .build();
    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .type("UPDATE_PROFILE")
            .userId(userId)
            .details(details)
            .build();

    when(userService.get(userId)).thenReturn(user);

    listener.handle(event);

    assertThat(user.getEmail()).isEqualTo("cd@example.com");
    verify(userService).update(user);
  }
}
