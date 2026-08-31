package vn.io.oldmoon.shopizer.user.business.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

  @Mock private CustomerProfileRepository customerProfileRepository;

  @Mock private UserService userService;

  @InjectMocks private CustomerProfileService customerProfileService;

  @Test
  @DisplayName("get(UUID keycloakUserId) should return CustomerProfile when exists")
  void get_WhenExists_ShouldReturnCustomerProfile() {
    // Given
    UUID profileId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    CustomerProfile expectedProfile =
        CustomerProfile.builder()
            .user(User.builder().keycloakUserId(keycloakUserId).build())
            .build();
    expectedProfile.setId(profileId);

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(new CustomerProfileQueryDto(profileId)));
    when(customerProfileRepository.findById(profileId)).thenReturn(Optional.of(expectedProfile));

    // When
    CustomerProfile result = customerProfileService.get(keycloakUserId);

    // Then
    assertThat(result).isNotNull().isEqualTo(expectedProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).findById(profileId);
  }

  @Test
  @DisplayName("get(UUID keycloakUserId) should throw ResourceNotFoundException when not found")
  void get_WhenNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> customerProfileService.get(keycloakUserId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Profile")
        .hasMessageContaining("userId=" + keycloakUserId);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
  }

  @Test
  @DisplayName("create(CustomerProfile) should save and return CustomerProfile when profile is new")
  void create_WhenValidProfile_ShouldSaveAndReturnProfile() {
    // Given
    UUID profileId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    CustomerProfile profile = CustomerProfile.builder().user(user).build();
    CustomerProfile savedProfile = CustomerProfile.builder().user(user).build();
    savedProfile.setId(profileId);

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());
    when(customerProfileRepository.save(profile)).thenReturn(savedProfile);

    // When
    CustomerProfile result = customerProfileService.create(profile);

    // Then
    assertThat(result).isNotNull().isEqualTo(savedProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).save(profile);
  }

  @Test
  @DisplayName("create(CustomerProfile) should return existing profile when already exists")
  void create_WhenAlreadyExists_ShouldReturnExistingProfile() {
    // Given
    UUID profileId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    CustomerProfile profile = CustomerProfile.builder().user(user).build();
    CustomerProfile existingProfile = CustomerProfile.builder().user(user).build();
    existingProfile.setId(profileId);

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(new CustomerProfileQueryDto(profileId)));
    when(customerProfileRepository.findById(profileId)).thenReturn(Optional.of(existingProfile));

    // When
    CustomerProfile result = customerProfileService.create(profile);

    // Then
    assertThat(result).isNotNull().isEqualTo(existingProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository, org.mockito.Mockito.never()).save(profile);
  }

  @Test
  @DisplayName("create(User) should create User via UserService and save CustomerProfile")
  void create_WithUser_ShouldCreateUserAndProfile() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    User inputUser =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("johndoe")
            .email("john@example.com")
            .build();
    User savedUser =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username("johndoe")
            .email("john@example.com")
            .build();
    savedUser.setId(userId);

    UUID profileId = UUID.randomUUID();
    CustomerProfile expectedSavedProfile = CustomerProfile.builder().user(savedUser).build();
    expectedSavedProfile.setId(profileId);

    when(userService.create(inputUser)).thenReturn(savedUser);
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());
    when(customerProfileRepository.save(org.mockito.ArgumentMatchers.any(CustomerProfile.class)))
        .thenReturn(expectedSavedProfile);

    // When
    CustomerProfile result = customerProfileService.create(inputUser);

    // Then
    assertThat(result).isNotNull().isEqualTo(expectedSavedProfile);
    verify(userService).create(inputUser);

    ArgumentCaptor<CustomerProfile> profileCaptor = ArgumentCaptor.forClass(CustomerProfile.class);
    verify(customerProfileRepository).save(profileCaptor.capture());
    CustomerProfile capturedProfile = profileCaptor.getValue();
    assertThat(capturedProfile.getUser()).isEqualTo(savedUser);
  }

  @Test
  @DisplayName("create(User) should throw NullPointerException when user is null")
  void create_WithUser_WhenNullUser_ShouldThrowNpe() {
    assertThatThrownBy(() -> customerProfileService.create((User) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("update(CustomerProfile) should save and return updated profile when id is present")
  void update_WithProfile_WhenIdPresent_ShouldSaveAndReturnProfile() {
    // Given
    UUID profileId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    CustomerProfile profile = CustomerProfile.builder().user(user).phoneNumber("+1234567890").build();
    profile.setId(profileId);

    when(customerProfileRepository.existsById(profileId)).thenReturn(true);
    when(customerProfileRepository.save(profile)).thenReturn(profile);

    // When
    CustomerProfile result = customerProfileService.update(profile);

    // Then
    assertThat(result).isNotNull().isEqualTo(profile);
    verify(customerProfileRepository).existsById(profileId);
    verify(customerProfileRepository).save(profile);
  }

  @Test
  @DisplayName("update(CustomerProfile) should throw InvalidInputException when profile has null id")
  void update_WithProfile_WhenNullId_ShouldThrowInvalidInputException() {
    // Given
    CustomerProfile profile = CustomerProfile.builder().build();

    // When & Then
    assertThatThrownBy(() -> customerProfileService.update(profile))
        .isInstanceOf(InvalidInputException.class)
        .hasMessage("Tried to update customer profile with invalid id");
  }

  @Test
  @DisplayName("update(CustomerProfile) should throw InvalidInputException when profile not found in repo")
  void update_WithProfile_WhenNotInRepo_ShouldThrowInvalidInputException() {
    // Given
    UUID profileId = UUID.randomUUID();
    CustomerProfile profile = CustomerProfile.builder().build();
    profile.setId(profileId);

    when(customerProfileRepository.existsById(profileId)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> customerProfileService.update(profile))
        .isInstanceOf(InvalidInputException.class)
        .hasMessage("Tried to update customer profile with invalid id");
  }

  @Test
  @DisplayName("update(CustomerProfile) should throw NullPointerException when profile is null")
  void update_WithProfile_WhenNull_ShouldThrowNpe() {
    assertThatThrownBy(() -> customerProfileService.update((CustomerProfile) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("update(User, CustomerProfile) should update user, associate with profile, and save profile")
  void update_WithUserAndProfile_ShouldUpdateUserAndSaveProfile() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    UUID keycloakUserId = UUID.randomUUID();

    User user = User.builder().keycloakUserId(keycloakUserId).firstName("Alice").build();
    user.setId(userId);
    User updatedUser = User.builder().keycloakUserId(keycloakUserId).firstName("AliceUpdated").build();
    updatedUser.setId(userId);

    CustomerProfile profile = CustomerProfile.builder().user(user).phoneNumber("+1234567890").build();
    profile.setId(profileId);

    when(userService.update(user)).thenReturn(updatedUser);
    when(customerProfileRepository.existsById(profileId)).thenReturn(true);
    when(customerProfileRepository.save(profile)).thenReturn(profile);

    // When
    CustomerProfile result = customerProfileService.update(user, profile);

    // Then
    assertThat(result).isNotNull().isEqualTo(profile);
    assertThat(profile.getUser()).isEqualTo(updatedUser);
    verify(userService).update(user);
    verify(customerProfileRepository).existsById(profileId);
    verify(customerProfileRepository).save(profile);
  }
}
