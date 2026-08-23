package vn.io.oldmoon.shopizer.user.business.event.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@ExtendWith(MockitoExtension.class)
class KeycloakUserRegisteredEventParserTest {

  @Mock private UserMapper userMapper;

  @InjectMocks private KeycloakUserRegisteredEventParser parser;

  @Test
  @DisplayName("Should delegate mapping to UserMapper and return converted User entity")
  void toUserEntity_WithValidEvent_ShouldReturnMappedUser() {
    UUID userId = UUID.randomUUID();
    KeycloakRegistrationDetails details =
        new KeycloakRegistrationDetails(
            "openid",
            "code",
            "form",
            "Doe",
            "http://localhost/callback",
            "John",
            "code-123",
            "john.doe@example.com",
            "johndoe");
    KeycloakUserRegisteredEvent event = new KeycloakUserRegisteredEvent(userId, details);

    User expectedUser =
        User.builder()
            .keycloakUserId(userId)
            .username("johndoe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();

    when(userMapper.toUserEntity(event)).thenReturn(expectedUser);

    User actualUser = parser.toUserEntity(event);

    assertThat(actualUser).isNotNull();
    assertThat(actualUser.getKeycloakUserId()).isEqualTo(userId);
    assertThat(actualUser.getUsername()).isEqualTo("johndoe");
    assertThat(actualUser.getEmail()).isEqualTo("john.doe@example.com");
    assertThat(actualUser.getFirstName()).isEqualTo("John");
    assertThat(actualUser.getLastName()).isEqualTo("Doe");

    verify(userMapper).toUserEntity(event);
  }
}
