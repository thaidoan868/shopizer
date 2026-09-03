package vn.io.oldmoon.shopizer.user.app.dto.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;

@ExtendWith(MockitoExtension.class)
class UserPopulatorTest {

  @Mock private UrlConvertService urlConvertService;

  @InjectMocks private UserPopulator userPopulator;

  @Test
  @DisplayName("toAvatarDto should return null when AvatarMeta is null")
  void toAvatarDto_NullAvatar_ShouldReturnNull() {
    assertThat(userPopulator.toAvatarDto(null)).isNull();
  }

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
}
