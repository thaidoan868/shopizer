package vn.io.oldmoon.shopizer.user.business.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
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
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerProfileDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

  @Mock private CustomerProfileRepository customerProfileRepository;

  @Mock private UserService userService;

  @Mock private UserPopulator userPopulator;

  @InjectMocks private CustomerProfileService customerProfileService;

  @Test
  @DisplayName("getCustomerProfile should return CustomerProfileDto when user and profile exist")
  void getCustomerProfile_WhenUserAndProfileExist_ShouldReturnDto() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).username("alice").build();
    CustomerProfile profile = CustomerProfile.builder().user(user).build();
    CustomerProfileQueryDto queryDto = new CustomerProfileQueryDto(profileId);
    CustomerProfileDto expectedDto = CustomerProfileDto.builder().username("alice").build();

    when(userService.get(keycloakUserId)).thenReturn(user);
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.of(queryDto));
    when(customerProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
    when(userPopulator.toCustomerProfileDto(user, profile)).thenReturn(expectedDto);

    // When
    CustomerProfileDto result = customerProfileService.getCustomerProfile(keycloakUserId);

    // Then
    assertThat(result).isNotNull().isEqualTo(expectedDto);
    verify(userService).get(keycloakUserId);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).findById(profileId);
    verify(userPopulator).toCustomerProfileDto(user, profile);
  }

  @Test
  @DisplayName(
      "getCustomerProfile should throw ResourceNotFoundException when User exists but CustomerProfile is missing")
  void getCustomerProfile_WhenUserExistsButProfileNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(keycloakUserId).username("alice").build();

    when(userService.get(keycloakUserId)).thenReturn(user);
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    // When & Then
    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> customerProfileService.getCustomerProfile(keycloakUserId))
        .isInstanceOf(vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException.class)
        .hasMessageContaining("CustomerProfile");

    verify(userService).get(keycloakUserId);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository, never()).findById(any());
    verify(userPopulator, never()).toCustomerProfileDto(any(), any());
  }

  @Test
  @DisplayName("getCustomerProfile should throw ResourceNotFoundException when User is not found")
  void getCustomerProfile_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    when(userService.get(keycloakUserId))
        .thenThrow(
            new vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException(
                "User", "userId=" + keycloakUserId));

    // When & Then
    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> customerProfileService.getCustomerProfile(keycloakUserId))
        .isInstanceOf(vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException.class)
        .hasMessageContaining("User");

    verify(userService).get(keycloakUserId);
    verify(customerProfileRepository, never()).findByKeycloakUserId(any());
  }

  @Test
  @DisplayName("get should return Optional containing Customer profile when found")
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
    Optional<CustomerProfile> actualProfile = customerProfileService.get(keycloakUserId);

    // Then
    assertThat(actualProfile).isPresent().contains(expectedProfile);
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository).findById(profileId);
  }

  @Test
  @DisplayName("get should return empty Optional when user is not found")
  void get_WhenNotFound_ShouldReturnEmptyOptional() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    when(customerProfileRepository.findByKeycloakUserId(keycloakUserId))
        .thenReturn(Optional.empty());

    // When
    Optional<CustomerProfile> actualProfile = customerProfileService.get(keycloakUserId);

    // Then
    assertThat(actualProfile).isEmpty();
    verify(customerProfileRepository).findByKeycloakUserId(keycloakUserId);
    verify(customerProfileRepository, never()).findById(any());
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
