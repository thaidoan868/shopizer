package vn.io.oldmoon.shopizer.user.app.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.model.profile.Address;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@ExtendWith(MockitoExtension.class)
class CustomerPopulatorTest {

  @Mock private CustomerMapper customerMapper;

  @Mock private UserPopulator userPopulator;

  @InjectMocks private CustomerPopulator customerPopulator;

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

    Address address =
        Address.builder()
            .detailsAddress("123 Main Street")
            .wardOrCommune("Ward Sai Gon")
            .cityOrProvince("HCM")
            .country("VN")
            .build();

    CustomerProfile profile =
        CustomerProfile.builder()
            .user(user)
            .gender(Gender.female)
            .dateOfBirth(LocalDate.of(1995, 5, 20))
            .language(Language.en)
            .phoneNumber("+1234567890")
            .address(address)
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
            .address(address)
            .build();

    AvatarDto avatarDto =
        new AvatarDto(
            "http://cdn.example.com/test-bucket/orig.jpg",
            "http://cdn.example.com/test-bucket/med.jpg",
            "http://cdn.example.com/test-bucket/thumb.jpg");

    given(customerMapper.toCustomerProfileDto(user, profile)).willReturn(mockDto);
    given(userPopulator.toAvatarDto(avatarMeta)).willReturn(avatarDto);

    // When
    CustomerProfileDto result = customerPopulator.toCustomerProfileDto(user, profile);

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
    assertThat(result.getAddress()).isEqualTo(address);
    assertThat(result.getAvatarMeta()).isEqualTo(avatarDto);

    verify(customerMapper).toCustomerProfileDto(user, profile);
    verify(userPopulator).toAvatarDto(avatarMeta);
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
    CustomerProfileDto result = customerPopulator.toCustomerProfileDto(user, profile);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAvatarMeta()).isNull();
    verify(customerMapper).toCustomerProfileDto(user, profile);
  }

  @Test
  @DisplayName("toCustomerProfileDto should throw NullPointerException when user is null")
  void toCustomerProfileDto_WhenUserIsNull_ShouldThrowNpe() {
    CustomerProfile profile = CustomerProfile.builder().build();
    assertThatThrownBy(() -> customerPopulator.toCustomerProfileDto(null, profile))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("toCustomerProfileDto should throw NullPointerException when profile is null")
  void toCustomerProfileDto_WhenProfileIsNull_ShouldThrowNpe() {
    User user = User.builder().build();
    assertThatThrownBy(() -> customerPopulator.toCustomerProfileDto(user, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("update should delegate to CustomerMapper for both User and CustomerProfile")
  void update_ShouldDelegateToCustomerMapper() {
    // Given
    Address oldAddress = Address.builder().detailsAddress("Old Address").build();
    Address newAddress = Address.builder().detailsAddress("New Address").build();

    User user = User.builder().firstName("OldFirst").lastName("OldLast").build();
    CustomerProfile profile =
        CustomerProfile.builder().phoneNumber("+1234567890").address(oldAddress).build();
    UpdateCustomerDto dto =
        UpdateCustomerDto.builder()
            .firstName("NewFirst")
            .lastName("NewLast")
            .phoneNumber("+84987654321")
            .address(newAddress)
            .build();

    // When
    customerPopulator.update(user, profile, dto);

    // Then
    verify(customerMapper).updateUserFromDto(dto, user);
    verify(customerMapper).updateCustomerProfileFromDto(dto, profile);
    assertThat(profile.getAddress()).isEqualTo(newAddress);
  }

  @Test
  @DisplayName("update should throw NullPointerException when user is null")
  void update_WhenUserIsNull_ShouldThrowNpe() {
    CustomerProfile profile = CustomerProfile.builder().build();
    UpdateCustomerDto dto = UpdateCustomerDto.builder().build();
    assertThatThrownBy(() -> customerPopulator.update(null, profile, dto))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("update should throw NullPointerException when profile is null")
  void update_WhenProfileIsNull_ShouldThrowNpe() {
    User user = User.builder().build();
    UpdateCustomerDto dto = UpdateCustomerDto.builder().build();
    assertThatThrownBy(() -> customerPopulator.update(user, null, dto))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("update should throw NullPointerException when dto is null")
  void update_WhenDtoIsNull_ShouldThrowNpe() {
    User user = User.builder().build();
    CustomerProfile profile = CustomerProfile.builder().build();
    assertThatThrownBy(() -> customerPopulator.update(user, profile, null))
        .isInstanceOf(NullPointerException.class);
  }
}
