package vn.io.oldmoon.shopizer.user.app.dto.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerMapper;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerProfileDto;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@ExtendWith(MockitoExtension.class)
class UserPopulatorTest {

  @Mock private CustomerMapper customerMapper;

  @Mock private UrlConvertService urlConvertService;

  @InjectMocks private UserPopulator userPopulator;

  @Test
  @DisplayName("toCustomerProfileDto should map User and CustomerProfile with avatar URLs")
  void toCustomerProfileDto_WithAvatar_ShouldPopulateAvatarUrls() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    AvatarMeta avatarMeta = new AvatarMeta("test-bucket", "orig.jpg", "med.jpg", "thumb.jpg");

    User user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("alice")
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Smith")
            .verified(true)
            .avatarMeta(avatarMeta)
            .build();
    user.setId(userId);

    CustomerProfile profile =
        CustomerProfile.builder()
            .user(user)
            .gender(Gender.female)
            .dateOfBirth(LocalDate.of(1995, 5, 20))
            .language(Language.en)
            .phoneNumber("+1234567890")
            .address("123 Main Street")
            .build();

    CustomerProfileDto mockDto =
        CustomerProfileDto.builder()
            .id(userId)
            .keycloakUserId(keycloakUserId)
            .username("alice")
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Smith")
            .verified(true)
            .gender(Gender.female)
            .dateOfBirth(LocalDate.of(1995, 5, 20))
            .language(Language.en)
            .phoneNumber("+1234567890")
            .address("123 Main Street")
            .build();

    given(customerMapper.toCustomerProfileDto(user, profile)).willReturn(mockDto);
    given(urlConvertService.media("test-bucket", "orig.jpg"))
        .willReturn("http://cdn.example.com/test-bucket/orig.jpg");
    given(urlConvertService.media("test-bucket", "med.jpg"))
        .willReturn("http://cdn.example.com/test-bucket/med.jpg");
    given(urlConvertService.media("test-bucket", "thumb.jpg"))
        .willReturn("http://cdn.example.com/test-bucket/thumb.jpg");

    // When
    CustomerProfileDto result = userPopulator.toCustomerProfileDto(user, profile);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(result.getUsername()).isEqualTo("alice");
    assertThat(result.getEmail()).isEqualTo("alice@example.com");
    assertThat(result.getFirstName()).isEqualTo("Alice");
    assertThat(result.getLastName()).isEqualTo("Smith");
    assertThat(result.getVerified()).isTrue();
    assertThat(result.getGender()).isEqualTo(Gender.female);
    assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 20));
    assertThat(result.getLanguage()).isEqualTo(Language.en);
    assertThat(result.getPhoneNumber()).isEqualTo("+1234567890");
    assertThat(result.getAddress()).isEqualTo("123 Main Street");

    assertThat(result.getAvatarMeta()).isNotNull();
    assertThat(result.getAvatarMeta().originalAvatarUrl())
        .isEqualTo("http://cdn.example.com/test-bucket/orig.jpg");
    assertThat(result.getAvatarMeta().mediumAvatarUrl())
        .isEqualTo("http://cdn.example.com/test-bucket/med.jpg");
    assertThat(result.getAvatarMeta().thumbnailAvatarUrl())
        .isEqualTo("http://cdn.example.com/test-bucket/thumb.jpg");

    verify(customerMapper).toCustomerProfileDto(user, profile);
  }

  @Test
  @DisplayName("toCustomerProfileDto should handle null avatar gracefully")
  void toCustomerProfileDto_WithoutAvatar_ShouldHaveNullAvatarMeta() {
    // Given
    User user = User.builder().username("bob").avatarMeta(null).build();
    CustomerProfile profile = CustomerProfile.builder().user(user).build();
    CustomerProfileDto mockDto = CustomerProfileDto.builder().username("bob").build();

    given(customerMapper.toCustomerProfileDto(user, profile)).willReturn(mockDto);

    // When
    CustomerProfileDto result = userPopulator.toCustomerProfileDto(user, profile);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAvatarMeta()).isNull();
    verify(customerMapper).toCustomerProfileDto(user, profile);
  }

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
