package vn.io.oldmoon.shopizer.user.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Nested
  @DisplayName("get(UUID userId)")
  class GetTest {

    @Test
    @DisplayName("should return User when found by userId")
    void get_WhenUserExists_ShouldReturnUser() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      User expectedUser =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("johndoe")
              .email("john@example.com")
              .build();

      when(userRepository.findByKeycloakUserId(keycloakUserId))
          .thenReturn(Optional.of(expectedUser));

      // When
      User actualUser = userService.get(keycloakUserId);

      // Then
      assertThat(actualUser).isNotNull().isEqualTo(expectedUser);
      verify(userRepository).findByKeycloakUserId(keycloakUserId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when User does not exist")
    void get_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());

      // When / Then
      assertThatThrownBy(() -> userService.get(keycloakUserId))
          .isInstanceOf(ResourceNotFoundException.class);

      verify(userRepository).findByKeycloakUserId(keycloakUserId);
    }
  }

  @Nested
  @DisplayName("create(User user)")
  class CreateTest {

    @Test
    @DisplayName("should save and return created User")
    void create_ShouldSaveAndReturnUser() {
      // Given
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

      when(userRepository.save(inputUser)).thenReturn(savedUser);

      // When
      User result = userService.create(inputUser);

      // Then
      assertThat(result).isNotNull().isEqualTo(savedUser);
      verify(userRepository).save(inputUser);
    }
  }
}
