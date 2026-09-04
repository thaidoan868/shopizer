package vn.io.oldmoon.shopizer.user.business.event.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

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

@ExtendWith(MockitoExtension.class)
class KeycloakVerifyEmailEventListenerTest {

  @Mock private UserService userService;

  @InjectMocks private KeycloakVerifyEmailEventListener listener;

  @Test
  @DisplayName("handle should invoke userService.verifyEmail with userId and email from event")
  void handle_WhenValidEvent_ShouldCallUserServiceVerifyEmail() {
    // Given
    UUID userId = UUID.randomUUID();
    String email = "test@example.com";
    KeycloakVerifyEmailDetails details =
        KeycloakVerifyEmailDetails.builder()
            .email(email)
            .username("testuser")
            .action("verify-email")
            .build();
    KeycloakVerifyEmailEvent event =
        KeycloakVerifyEmailEvent.builder()
            .type("VERIFY_EMAIL")
            .userId(userId)
            .details(details)
            .build();

    // When
    listener.handle(event);

    // Then
    verify(userService).verifyEmail(userId, email);
  }

  @Test
  @DisplayName("handle should throw InvalidInputException when event is null")
  void handle_WhenEventNull_ShouldThrowInvalidInputException() {
    assertThatThrownBy(() -> listener.handle(null))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("must not be null");
  }

  @Test
  @DisplayName("handle should throw InvalidInputException when event userId is null")
  void handle_WhenUserIdNull_ShouldThrowInvalidInputException() {
    KeycloakVerifyEmailEvent event =
        KeycloakVerifyEmailEvent.builder().type("VERIFY_EMAIL").userId(null).build();

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("must not be null");
  }

  @Test
  @DisplayName("handle should throw InvalidInputException when event details is null")
  void handle_WhenDetailsNull_ShouldThrowInvalidInputException() {
    UUID userId = UUID.randomUUID();
    KeycloakVerifyEmailEvent event =
        KeycloakVerifyEmailEvent.builder().type("VERIFY_EMAIL").userId(userId).details(null).build();

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("details or email must not be null");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("handle should throw InvalidInputException when details email is null or blank")
  void handle_WhenDetailsEmailNullOrBlank_ShouldThrowInvalidInputException(String email) {
    UUID userId = UUID.randomUUID();
    KeycloakVerifyEmailDetails details =
        KeycloakVerifyEmailDetails.builder().email(email).username("testuser").build();
    KeycloakVerifyEmailEvent event =
        KeycloakVerifyEmailEvent.builder()
            .type("VERIFY_EMAIL")
            .userId(userId)
            .details(details)
            .build();

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("details or email must not be null");
  }
}
