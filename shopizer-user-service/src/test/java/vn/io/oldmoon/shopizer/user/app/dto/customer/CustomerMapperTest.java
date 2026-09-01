package vn.io.oldmoon.shopizer.user.app.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

class CustomerMapperTest {

  private final CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);

  @Test
  @DisplayName("toCustomerProfileDto should correctly map all fields from User and CustomerProfile")
  void toCustomerProfileDto_ShouldMapAllFields() {
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

    CustomerProfile profile =
        CustomerProfile.builder()
            .user(user)
            .gender(Gender.male)
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .language(Language.vn)
            .phoneNumber("+1987654321")
            .address("456 Elm St")
            .build();

    // When
    CustomerProfileDto dto = customerMapper.toCustomerProfileDto(user, profile);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(userId);
    assertThat(dto.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(dto.getUsername()).isEqualTo("johndoe");
    assertThat(dto.getEmail()).isEqualTo("johndoe@example.com");
    assertThat(dto.getFirstName()).isEqualTo("John");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    assertThat(dto.getVerified()).isTrue();
    assertThat(dto.getGender()).isEqualTo(Gender.male);
    assertThat(dto.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 15));
    assertThat(dto.getLanguage()).isEqualTo(Language.vn);
    assertThat(dto.getPhoneNumber()).isEqualTo("+1987654321");
    assertThat(dto.getAddress()).isEqualTo("456 Elm St");
    assertThat(dto.getAvatarMeta())
        .isNull(); // explicitly ignored in mapper, populated in populator
  }

  @Test
  @DisplayName("toCustomerProfileDto should return null when both inputs are null")
  void toCustomerProfileDto_WhenBothNull_ShouldReturnNull() {
    CustomerProfileDto dto = customerMapper.toCustomerProfileDto(null, null);
    assertThat(dto).isNull();
  }

  @Test
  @DisplayName("updateUserFromDto should update only non-null user fields from DTO")
  void updateUserFromDto_ShouldUpdateNonNullFieldsOnly() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    User user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("johndoe")
            .email("johndoe@example.com")
            .firstName("OldFirst")
            .lastName("OldLast")
            .verified(true)
            .build();
    user.setId(userId);

    UpdateCustomerDto dto =
        UpdateCustomerDto.builder().firstName("NewFirst").lastName("NewLast").build();

    // When
    customerMapper.updateUserFromDto(dto, user);

    // Then
    assertThat(user.getFirstName()).isEqualTo("NewFirst");
    assertThat(user.getLastName()).isEqualTo("NewLast");
    assertThat(user.getId()).isEqualTo(userId);
    assertThat(user.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(user.getUsername()).isEqualTo("johndoe");
    assertThat(user.getEmail()).isEqualTo("johndoe@example.com");
    assertThat(user.getVerified()).isTrue();
  }

  @Test
  @DisplayName("updateCustomerProfileFromDto should update non-null profile fields from DTO")
  void updateCustomerProfileFromDto_ShouldUpdateNonNullFieldsOnly() {
    // Given
    UUID profileId = UUID.randomUUID();
    CustomerProfile profile =
        CustomerProfile.builder()
            .gender(Gender.male)
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .language(Language.en)
            .phoneNumber("+1234567890")
            .address("Old Address")
            .build();
    profile.setId(profileId);

    UpdateCustomerDto dto =
        UpdateCustomerDto.builder()
            .gender(Gender.female)
            .dateOfBirth(LocalDate.of(1995, 5, 20))
            .language(Language.vn)
            .phoneNumber("+84987654321")
            .address("New Address")
            .build();

    // When
    customerMapper.updateCustomerProfileFromDto(dto, profile);

    // Then
    assertThat(profile.getId()).isEqualTo(profileId);
    assertThat(profile.getGender()).isEqualTo(Gender.female);
    assertThat(profile.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 20));
    assertThat(profile.getLanguage()).isEqualTo(Language.vn);
    assertThat(profile.getPhoneNumber()).isEqualTo("+84987654321");
    assertThat(profile.getAddress()).isEqualTo("New Address");
  }
}
