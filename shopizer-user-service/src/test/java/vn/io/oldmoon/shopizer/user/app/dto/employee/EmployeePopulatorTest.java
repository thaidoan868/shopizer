package vn.io.oldmoon.shopizer.user.app.dto.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.model.profile.Shift;

@ExtendWith(MockitoExtension.class)
class EmployeePopulatorTest {

  @Mock private EmployeeMapper employeeMapper;

  @Mock private UserPopulator userPopulator;

  @InjectMocks private EmployeePopulator employeePopulator;

  @Test
  @DisplayName("toEmployeeProfileDto should map User and EmployeeProfile with avatar URLs")
  void toEmployeeProfileDto_WithAvatar_ShouldPopulateAvatarUrls() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    AvatarMeta avatarMeta = new AvatarMeta("test-bucket", "orig.jpg", "med.jpg", "thumb.jpg");

    User user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("emp_user")
            .email("emp@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .verified(true)
            .avatarMeta(avatarMeta)
            .build();
    user.setId(userId);

    EmployeeProfile profile =
        EmployeeProfile.builder()
            .user(user)
            .shift(Shift.AFTERNOON)
            .workPhone("+1234567890")
            .build();

    EmployeeProfileDto mockDto =
        EmployeeProfileDto.builder()
            .id(userId)
            .keycloakUserId(keycloakUserId)
            .username("emp_user")
            .email("emp@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .verified(true)
            .shift(Shift.AFTERNOON)
            .workPhone("+1234567890")
            .build();

    AvatarDto avatarDto =
        new AvatarDto(
            "http://cdn.example.com/test-bucket/orig.jpg",
            "http://cdn.example.com/test-bucket/med.jpg",
            "http://cdn.example.com/test-bucket/thumb.jpg");

    given(employeeMapper.toEmployeeProfileDto(user, profile)).willReturn(mockDto);
    given(userPopulator.toAvatarDto(avatarMeta)).willReturn(avatarDto);

    // When
    EmployeeProfileDto result = employeePopulator.toEmployeeProfileDto(user, profile);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getKeycloakUserId()).isEqualTo(keycloakUserId);
    assertThat(result.getUsername()).isEqualTo("emp_user");
    assertThat(result.getEmail()).isEqualTo("emp@example.com");
    assertThat(result.getFirstName()).isEqualTo("Jane");
    assertThat(result.getLastName()).isEqualTo("Doe");
    assertThat(result.getVerified()).isTrue();
    assertThat(result.getShift()).isEqualTo(Shift.AFTERNOON);
    assertThat(result.getWorkPhone()).isEqualTo("+1234567890");
    assertThat(result.getAvatarMeta()).isEqualTo(avatarDto);

    verify(employeeMapper).toEmployeeProfileDto(user, profile);
    verify(userPopulator).toAvatarDto(avatarMeta);
  }

  @Test
  @DisplayName("toEmployeeProfileDto should handle null avatar gracefully")
  void toEmployeeProfileDto_WithoutAvatar_ShouldHaveNullAvatarMeta() {
    // Given
    User user = User.builder().username("emp_no_avatar").avatarMeta(null).build();
    EmployeeProfile profile = EmployeeProfile.builder().user(user).build();
    EmployeeProfileDto mockDto = EmployeeProfileDto.builder().username("emp_no_avatar").build();

    given(employeeMapper.toEmployeeProfileDto(user, profile)).willReturn(mockDto);

    // When
    EmployeeProfileDto result = employeePopulator.toEmployeeProfileDto(user, profile);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAvatarMeta()).isNull();
    verify(employeeMapper).toEmployeeProfileDto(user, profile);
  }

  @Test
  @DisplayName("toEmployeeProfileDto should throw NullPointerException when user is null")
  void toEmployeeProfileDto_WhenUserIsNull_ShouldThrowNpe() {
    EmployeeProfile profile = EmployeeProfile.builder().build();
    assertThatThrownBy(() -> employeePopulator.toEmployeeProfileDto(null, profile))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("toEmployeeProfileDto should throw NullPointerException when profile is null")
  void toEmployeeProfileDto_WhenProfileIsNull_ShouldThrowNpe() {
    User user = User.builder().build();
    assertThatThrownBy(() -> employeePopulator.toEmployeeProfileDto(user, null))
        .isInstanceOf(NullPointerException.class);
  }
}
