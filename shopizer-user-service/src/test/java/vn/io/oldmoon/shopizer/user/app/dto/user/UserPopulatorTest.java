package vn.io.oldmoon.shopizer.user.app.dto.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@ExtendWith(MockitoExtension.class)
class UserPopulatorTest {

  @Mock private UserMapper userMapper;
  @Mock private UrlConvertService urlConvertService;

  @InjectMocks private UserPopulator userPopulator;

  @Test
  @DisplayName("toAvatarDto should convert non-null AvatarMeta correctly")
  void toAvatarDto_ValidAvatar_ShouldReturnAvatarDto() {
    AvatarMeta avatarMeta = new AvatarMeta("bucket", "o.png", "m.png", "t.png");
    given(urlConvertService.media("bucket", "o.png")).willReturn("http://cdn/o.png");
    given(urlConvertService.media("bucket", "m.png")).willReturn("http://cdn/m.png");
    given(urlConvertService.media("bucket", "t.png")).willReturn("http://cdn/t.png");

    AvatarDto avatarDto = userPopulator.toAvatarDto(avatarMeta);

    assertThat(avatarDto).isNotNull();
    assertThat(avatarDto.originalAvatarUrl()).isEqualTo("http://cdn/o.png");
    assertThat(avatarDto.mediumAvatarUrl()).isEqualTo("http://cdn/m.png");
    assertThat(avatarDto.thumbnailAvatarUrl()).isEqualTo("http://cdn/t.png");
  }

  @Test
  @DisplayName("toUserDto should convert User with avatarMeta correctly")
  void toUserDto_ValidUserWithAvatar_ShouldReturnUserDto() {
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    AvatarMeta avatarMeta = new AvatarMeta("bucket", "o.png", "m.png", "t.png");
    User user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("john_doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .verified(true)
            .avatarMeta(avatarMeta)
            .build();
    user.setId(userId);

    UserDto mappedDto =
        UserDto.builder()
            .id(userId)
            .keycloakUserId(keycloakUserId)
            .username("john_doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .verified(true)
            .build();

    given(userMapper.toUserDto(user)).willReturn(mappedDto);
    given(urlConvertService.media("bucket", "o.png")).willReturn("http://cdn/o.png");
    given(urlConvertService.media("bucket", "m.png")).willReturn("http://cdn/m.png");
    given(urlConvertService.media("bucket", "t.png")).willReturn("http://cdn/t.png");

    UserDto userDto = userPopulator.toUserDto(user);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getId()).isEqualTo(userId);
    assertThat(userDto.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(userDto.getUsername()).isEqualTo("john_doe");
    assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    assertThat(userDto.getFirstName()).isEqualTo("John");
    assertThat(userDto.getLastName()).isEqualTo("Doe");
    assertThat(userDto.getVerified()).isTrue();
    assertThat(userDto.getAvatarMeta()).isNotNull();
    assertThat(userDto.getAvatarMeta().originalAvatarUrl()).isEqualTo("http://cdn/o.png");
  }

  @Test
  @DisplayName("toUserDto should convert User without avatarMeta correctly")
  void toUserDto_ValidUserWithoutAvatar_ShouldReturnUserDto() {
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    User user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("john_doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .verified(false)
            .build();
    user.setId(userId);

    UserDto mappedDto =
        UserDto.builder()
            .id(userId)
            .keycloakUserId(keycloakUserId)
            .username("john_doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .verified(false)
            .build();

    given(userMapper.toUserDto(user)).willReturn(mappedDto);

    UserDto userDto = userPopulator.toUserDto(user);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getId()).isEqualTo(userId);
    assertThat(userDto.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(userDto.getAvatarMeta()).isNull();
  }
}
