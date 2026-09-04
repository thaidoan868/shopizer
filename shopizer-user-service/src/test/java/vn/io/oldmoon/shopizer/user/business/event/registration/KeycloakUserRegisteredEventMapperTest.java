package vn.io.oldmoon.shopizer.user.business.event.registration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

class KeycloakUserRegisteredEventMapperTest {

  private final KeycloakUserRegisteredEventMapper mapper =
      Mappers.getMapper(KeycloakUserRegisteredEventMapper.class);

  @Test
  @DisplayName("toUserEntity should return null when eventDto is null")
  void toUserEntity_WhenNull_ShouldReturnNull() {
    assertThat(mapper.toUserEntity(null)).isNull();
  }

  @Test
  @DisplayName("toUserEntity should map all fields from event and details to User")
  void toUserEntity_WhenValidEvent_ShouldMapFields() {
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

    User user = mapper.toUserEntity(event);

    assertThat(user).isNotNull();
    assertThat(user.getKeycloakUserId()).isEqualTo(userId);
    assertThat(user.getUsername()).isEqualTo("johndoe");
    assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    assertThat(user.getFirstName()).isEqualTo("John");
    assertThat(user.getLastName()).isEqualTo("Doe");
    assertThat(user.getRealm()).isEqualTo("shopizer");
    assertThat(user.getVerified()).isFalse();
    assertThat(user.getAvatarMeta()).isNull();
  }
}
