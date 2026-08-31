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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
  @DisplayName("get should return CustomerProfile when found")
  void get_WhenFound_ShouldReturnCustomerProfile() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    CustomerProfileQueryDto queryDto = new CustomerProfileQueryDto(profileId);
    CustomerProfile expectedProfile = mock(CustomerProfile.class);

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(customerProfileRepository.findById(profileId)).thenReturn(Optional.of(expectedProfile));

    // When
    CustomerProfile actualProfile = customerProfileService.get(keycloakUserId);

    // Then
    assertThat(actualProfile).isNotNull().isEqualTo(expectedProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).findById(profileId);
  }

  @Test
  @DisplayName("get should throw ResourceNotFoundException when user is not found")
  void get_WhenNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> customerProfileService.get(keycloakUserId))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository, never()).findById(any());
  }

  @Test
  @DisplayName("get should throw ResourceNotFoundException when profile ID is not found")
  void get_WhenProfileIdNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    CustomerProfileQueryDto queryDto = new CustomerProfileQueryDto(profileId);

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(customerProfileRepository.findById(profileId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> customerProfileService.get(keycloakUserId))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).findById(profileId);
  }

  @Test
  @DisplayName(
      "create(CustomerProfile) should save and return CustomerProfile when profile does not exist")
  void create_WithCustomerProfile_WhenNotExist_ShouldSaveAndReturnProfile() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    CustomerProfile inputProfile = CustomerProfile.builder().user(user).build();

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());
    when(customerProfileRepository.save(inputProfile)).thenReturn(inputProfile);

    // When
    CustomerProfile result = customerProfileService.create(inputProfile);

    // Then
    assertThat(result).isNotNull().isEqualTo(inputProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).save(inputProfile);
  }

  @Test
  @DisplayName(
      "create(CustomerProfile) should return existing profile and skip saving when profile already exists")
  void create_WithCustomerProfile_WhenExists_ShouldReturnExistingProfileAndSkipSave() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    CustomerProfile inputProfile = CustomerProfile.builder().user(user).build();

    CustomerProfileQueryDto queryDto = new CustomerProfileQueryDto(profileId);
    CustomerProfile existingProfile = CustomerProfile.builder().user(user).build();
    existingProfile.setId(profileId);

    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(customerProfileRepository.findById(profileId)).thenReturn(Optional.of(existingProfile));

    // When
    CustomerProfile result = customerProfileService.create(inputProfile);

    // Then
    assertThat(result).isNotNull().isEqualTo(existingProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).findById(profileId);
    verify(customerProfileRepository, never()).save(any());
  }

  @Test
  @DisplayName(
      "create(User) should save User via UserService, construct CustomerProfile, and delegate to create(CustomerProfile)")
  void create_WithUnsavedUser_ShouldCreateUserAndSaveProfile() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    User unsavedUser = mock(User.class);
    User savedUser = User.builder().keycloakUserId(keycloakUserId).build();

    when(userService.create(unsavedUser)).thenReturn(savedUser);
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    CustomerProfile expectedSavedProfile = mock(CustomerProfile.class);
    when(customerProfileRepository.save(any(CustomerProfile.class)))
        .thenReturn(expectedSavedProfile);

    // When
    CustomerProfile result = customerProfileService.create(unsavedUser);

    // Then
    assertThat(result).isEqualTo(expectedSavedProfile);
    verify(userService).create(unsavedUser);

    ArgumentCaptor<CustomerProfile> profileCaptor = ArgumentCaptor.forClass(CustomerProfile.class);
    verify(customerProfileRepository).save(profileCaptor.capture());

    CustomerProfile capturedProfile = profileCaptor.getValue();
    assertThat(capturedProfile.getUser()).isEqualTo(savedUser);
  }
}
