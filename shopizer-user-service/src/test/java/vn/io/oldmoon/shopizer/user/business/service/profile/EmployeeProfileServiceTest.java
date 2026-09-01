package vn.io.oldmoon.shopizer.user.business.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeProfileServiceTest {

  @Mock private EmployeeProfileRepository employeeProfileRepository;
  @Mock private UserService userService;

  @InjectMocks private EmployeeProfileService employeeProfileService;

  @Test
  @DisplayName("get should return EmployeeProfile when found")
  void get_WhenFound_ShouldReturnEmployeeProfile() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    EmployeeProfileQueryDto queryDto = new EmployeeProfileQueryDto(profileId);
    EmployeeProfile expectedProfile = mock(EmployeeProfile.class);

    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(employeeProfileRepository.findById(profileId)).thenReturn(Optional.of(expectedProfile));

    // When
    EmployeeProfile actualProfile = employeeProfileService.get(keycloakUserId);

    // Then
    assertThat(actualProfile).isNotNull().isEqualTo(expectedProfile);
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(employeeProfileRepository).findById(profileId);
  }

  @Test
  @DisplayName("get should throw ResourceNotFoundException when employee profile is not found")
  void get_WhenNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> employeeProfileService.get(keycloakUserId))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(employeeProfileRepository, never()).findById(any());
  }

  @Test
  @DisplayName("get should throw ResourceNotFoundException when employee profile ID is not found")
  void get_WhenProfileIdNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    EmployeeProfileQueryDto queryDto = new EmployeeProfileQueryDto(profileId);

    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(employeeProfileRepository.findById(profileId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> employeeProfileService.get(keycloakUserId))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(employeeProfileRepository).findById(profileId);
  }

  @Test
  @DisplayName("exists should return true when profile exists")
  void exists_WhenExists_ShouldReturnTrue() {
    UUID keycloakUserId = UUID.randomUUID();
    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(new EmployeeProfileQueryDto(UUID.randomUUID())));

    boolean result = employeeProfileService.exists(keycloakUserId);

    assertThat(result).isTrue();
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
  }

  @Test
  @DisplayName("exists should return false when profile does not exist")
  void exists_WhenDoesNotExist_ShouldReturnFalse() {
    UUID keycloakUserId = UUID.randomUUID();
    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    boolean result = employeeProfileService.exists(keycloakUserId);

    assertThat(result).isFalse();
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
  }

  @Test
  @DisplayName(
      "create(EmployeeProfile) should save and return EmployeeProfile when profile does not exist")
  void create_WithEmployeeProfile_WhenNotExist_ShouldSaveAndReturnProfile() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    EmployeeProfile inputProfile = EmployeeProfile.builder().user(user).build();

    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());
    when(employeeProfileRepository.save(inputProfile)).thenReturn(inputProfile);

    // When
    EmployeeProfile result = employeeProfileService.create(inputProfile);

    // Then
    assertThat(result).isNotNull().isEqualTo(inputProfile);
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(employeeProfileRepository).save(inputProfile);
  }

  @Test
  @DisplayName(
      "create(EmployeeProfile) should return existing profile and skip saving when profile already exists")
  void create_WithEmployeeProfile_WhenExists_ShouldReturnExistingProfileAndSkipSave() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    EmployeeProfile inputProfile = EmployeeProfile.builder().user(user).build();

    EmployeeProfileQueryDto queryDto = new EmployeeProfileQueryDto(profileId);
    EmployeeProfile existingProfile = EmployeeProfile.builder().user(user).build();
    existingProfile.setId(profileId);

    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(employeeProfileRepository.findById(profileId)).thenReturn(Optional.of(existingProfile));

    // When
    EmployeeProfile result = employeeProfileService.create(inputProfile);

    // Then
    assertThat(result).isNotNull().isEqualTo(existingProfile);
    verify(employeeProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(employeeProfileRepository).findById(profileId);
    verify(employeeProfileRepository, never()).save(any());
  }

  @Test
  @DisplayName("create(User) should save user and create employee profile")
  void create_WithUser_ShouldSaveUserAndCreateProfile() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID creatorId = UUID.randomUUID();
    User notSavedUser =
        User.builder().keycloakUserId(keycloakUserId).username("admin").build();
    notSavedUser.setCreatedBy(creatorId);

    User savedUser =
        User.builder().keycloakUserId(keycloakUserId).username("admin").build();
    savedUser.setId(UUID.randomUUID());
    savedUser.setCreatedBy(creatorId);

    when(userService.create(notSavedUser)).thenReturn(savedUser);
    when(employeeProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());
    when(employeeProfileRepository.save(any(EmployeeProfile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    EmployeeProfile result = employeeProfileService.create(notSavedUser);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(savedUser);
    assertThat(result.getCreatedBy()).isEqualTo(creatorId);
    verify(userService).create(notSavedUser);
    verify(employeeProfileRepository).save(any(EmployeeProfile.class));
  }

  @Test
  @DisplayName("create(User) should throw NullPointerException when user is null")
  void create_WithNullUser_ShouldThrowNullPointerException() {
    assertThatThrownBy(() -> employeeProfileService.create((User) null))
        .isInstanceOf(NullPointerException.class);
  }
}
