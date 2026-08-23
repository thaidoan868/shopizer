package vn.io.oldmoon.shopizer.user.business.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

  @Mock private CustomerProfileRepository customerProfileRepository;

  @Mock private UserService userService;

  @InjectMocks private CustomerProfileService customerProfileService;

  @Test
  @DisplayName("getSupportedRole should return Role.CUSTOMER")
  void getSupportedRole_ShouldReturnCustomer() {
    assertThat(customerProfileService.getSupportedRole()).isEqualTo(Role.CUSTOMER);
  }

  @Test
  @DisplayName("update should return null")
  void update_ShouldReturnNull() {
    assertThat(customerProfileService.update()).isNull();
  }

  @Test
  @DisplayName("get should return Customer profile when found")
  void get_WhenFound_ShouldReturnUser() {
    // Given
    UUID userId = UUID.randomUUID();
    CustomerProfile expectedProfile = mock(CustomerProfile.class);
    when(customerProfileRepository.findByKeycloakUserId(userId))
        .thenReturn(Optional.of(expectedProfile));

    // When
    CustomerProfile actualProfile = customerProfileService.get(userId);

    // Then
    assertThat(actualProfile).isNotNull().isEqualTo(expectedProfile);
    verify(customerProfileRepository).findByKeycloakUserId(userId);
  }

  @Test
  @DisplayName("get should throw ResourceNotFoundException when user is not found")
  void get_WhenNotFound_ShouldThrowResourceNotFoundException() {
    // Given
    UUID userId = UUID.randomUUID();
    when(customerProfileRepository.findByKeycloakUserId(userId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> customerProfileService.get(userId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("create(CustomerProfile) should save and return CustomerProfile")
  void create_WithCustomerProfile_ShouldSaveAndReturnProfile() {
    // Given
    UUID userId = UUID.randomUUID();
    CustomerProfile inputProfile = CustomerProfile.builder().keycloakUserId(userId).build();

    CustomerProfile expectedSavedProfile = CustomerProfile.builder().keycloakUserId(userId).build();

    when(customerProfileRepository.save(inputProfile)).thenReturn(expectedSavedProfile);

    // When
    CustomerProfile result = customerProfileService.create(inputProfile);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getKeycloakUserId()).isEqualTo(userId);
    verify(customerProfileRepository).save(inputProfile);
  }

  @Test
  @DisplayName(
      "create(User) should save User via UserService, construct CustomerProfile, and save it")
  void create_WithUnsavedUser_ShouldCreateUserAndSaveProfile() {
    // Given
    UUID userId = UUID.randomUUID();
    User unsavedUser = mock(User.class);
    User savedUser = mock(User.class);
    CustomerProfile savedProfile = mock(CustomerProfile.class);

    when(savedUser.getKeycloakUserId()).thenReturn(userId);
    when(userService.create(unsavedUser)).thenReturn(savedUser);
    when(customerProfileRepository.save(any(CustomerProfile.class))).thenReturn(savedProfile);

    // When
    CustomerProfile result = customerProfileService.create(unsavedUser);

    // Then
    assertThat(result).isEqualTo(savedProfile);
    verify(userService).create(unsavedUser);

    ArgumentCaptor<CustomerProfile> profileCaptor = ArgumentCaptor.forClass(CustomerProfile.class);
    verify(customerProfileRepository).save(profileCaptor.capture());

    CustomerProfile capturedProfile = profileCaptor.getValue();
    assertThat(capturedProfile.getUser()).isEqualTo(savedUser);
    assertThat(capturedProfile.getKeycloakUserId()).isEqualTo(userId);
  }
}
