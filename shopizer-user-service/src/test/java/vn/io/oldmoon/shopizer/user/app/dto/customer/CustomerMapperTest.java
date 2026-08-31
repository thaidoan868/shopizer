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
    assertThat(dto.getAvatarMeta()).isNull(); // explicitly ignored in mapper, populated in populator
  }

  @Test
  @DisplayName("toCustomerProfileDto should return null when both inputs are null")
  void toCustomerProfileDto_WhenBothNull_ShouldReturnNull() {
    CustomerProfileDto dto = customerMapper.toCustomerProfileDto(null, null);
    assertThat(dto).isNull();
  }
}
