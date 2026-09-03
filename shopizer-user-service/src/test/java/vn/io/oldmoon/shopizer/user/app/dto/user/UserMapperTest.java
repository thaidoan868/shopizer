package vn.io.oldmoon.shopizer.user.app.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

class UserMapperTest {

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @Test
  @DisplayName("toUserDto should return null when user is null")
  void toUserDto_NullUser_ShouldReturnNull() {
    assertThat(userMapper.toUserDto(null)).isNull();
  }

  @Test
  @DisplayName("toUserDto should correctly map all fields from User except avatarMeta which is ignored")
  void toUserDto_ShouldMapAllFields() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    AvatarMeta avatarMeta = new AvatarMeta("bucket", "orig.jpg", "med.jpg", "thumb.jpg");

    User user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("johndoe")
            .email("johndoe@example.com")
            .firstName("John")
            .lastName("Doe")
            .verified(true)
            .avatarMeta(avatarMeta)
            .build();
    user.setId(userId);

    // When
    UserDto dto = userMapper.toUserDto(user);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(userId);
    assertThat(dto.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(dto.getUsername()).isEqualTo("johndoe");
    assertThat(dto.getEmail()).isEqualTo("johndoe@example.com");
    assertThat(dto.getFirstName()).isEqualTo("John");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    assertThat(dto.getVerified()).isTrue();
    assertThat(dto.getAvatarMeta()).isNull();
  }
}
