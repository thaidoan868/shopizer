package vn.io.oldmoon.shopizer.user.app.dto.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.model.profile.Shift;

class EmployeeMapperTest {

  private final EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

  @Test
  @DisplayName("toEmployeeProfileDto should correctly map all fields from User and EmployeeProfile")
  void toEmployeeProfileDto_ShouldMapAllFields() {
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

    EmployeeProfile profile =
        EmployeeProfile.builder()
            .user(user)
            .shift(Shift.MORNING)
            .workPhone("+1987654321")
            .build();

    // When
    EmployeeProfileDto dto = employeeMapper.toEmployeeProfileDto(user, profile);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(userId);
    assertThat(dto.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(dto.getUsername()).isEqualTo("johndoe");
    assertThat(dto.getEmail()).isEqualTo("johndoe@example.com");
    assertThat(dto.getFirstName()).isEqualTo("John");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    assertThat(dto.getVerified()).isTrue();
    assertThat(dto.getShift()).isEqualTo(Shift.MORNING);
    assertThat(dto.getWorkPhone()).isEqualTo("+1987654321");
    assertThat(dto.getAvatarMeta())
        .isNull(); // explicitly ignored in mapper, populated in populator
  }

  @Test
  @DisplayName("toEmployeeProfileDto should return null when both inputs are null")
  void toEmployeeProfileDto_WhenBothNull_ShouldReturnNull() {
    EmployeeProfileDto dto = employeeMapper.toEmployeeProfileDto(null, null);
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

    UpdateEmployeeDto dto =
        UpdateEmployeeDto.builder().firstName("NewFirst").lastName("NewLast").build();

    // When
    employeeMapper.updateUserFromDto(dto, user);

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
  @DisplayName("updateEmployeeProfileFromDto should update non-null profile fields from DTO")
  void updateEmployeeProfileFromDto_ShouldUpdateNonNullFieldsOnly() {
    // Given
    UUID profileId = UUID.randomUUID();
    EmployeeProfile profile =
        EmployeeProfile.builder()
            .shift(Shift.MORNING)
            .workPhone("+1234567890")
            .build();
    profile.setId(profileId);

    UpdateEmployeeDto dto =
        UpdateEmployeeDto.builder()
            .shift(Shift.NIGHT)
            .workPhone("+84987654321")
            .build();

    // When
    employeeMapper.updateEmployeeProfileFromDto(dto, profile);

    // Then
    assertThat(profile.getId()).isEqualTo(profileId);
    assertThat(profile.getShift()).isEqualTo(Shift.NIGHT);
    assertThat(profile.getWorkPhone()).isEqualTo("+84987654321");
  }
}

