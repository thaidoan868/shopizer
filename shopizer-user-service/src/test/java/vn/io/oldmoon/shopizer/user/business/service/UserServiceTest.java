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
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Nested
  @DisplayName("get(UUID keycloakUserId)")
  class GetTest {

    @Test
    @DisplayName("should return user when user exists with given keycloakUserId")
    void get_WhenUserExists_ShouldReturnUser() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      User user = User.builder().keycloakUserId(keycloakUserId).username("johndoe").build();
      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(user));

      // When
      User result = userService.get(keycloakUserId);

      // Then
      assertThat(result).isNotNull().isEqualTo(user);
      verify(userRepository).findByKeycloakUserId(keycloakUserId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user does not exist")
    void get_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.get(keycloakUserId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User")
          .hasMessageContaining("userId=" + keycloakUserId);
      verify(userRepository).findByKeycloakUserId(keycloakUserId);
    }

    @Test
    @DisplayName("should throw NullPointerException when keycloakUserId is null")
    void get_WhenKeycloakUserIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> userService.get(null)).isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("create(User user)")
  class CreateTest {

    @Test
    @DisplayName("should save and return new user when user does not exist")
    void create_WhenUserDoesNotExist_ShouldSaveAndReturnUser() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      User inputUser =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("janedoe")
              .email("jane@example.com")
              .build();
      User savedUser =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("janedoe")
              .email("jane@example.com")
              .build();

      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());
      when(userRepository.save(inputUser)).thenReturn(savedUser);

      // When
      User result = userService.create(inputUser);

      // Then
      assertThat(result).isNotNull().isEqualTo(savedUser);
      verify(userRepository).findByKeycloakUserId(keycloakUserId);
      verify(userRepository).save(inputUser);
    }

    @Test
    @DisplayName(
        "should return existing user without saving when user with keycloakUserId already exists")
    void create_WhenUserAlreadyExists_ShouldReturnExistingUser() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      User inputUser =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("janedoe")
              .email("jane@example.com")
              .build();
      User existingUser =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("janedoe")
              .email("jane@example.com")
              .build();

      when(userRepository.findByKeycloakUserId(keycloakUserId))
          .thenReturn(Optional.of(existingUser));

      // When
      User result = userService.create(inputUser);

      // Then
      assertThat(result).isNotNull().isEqualTo(existingUser);
      verify(userRepository).findByKeycloakUserId(keycloakUserId);
      org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.never()).save(inputUser);
    }

    @Test
    @DisplayName("should throw NullPointerException when user is null")
    void create_WhenUserIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> userService.create(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should save user directly when keycloakUserId is null")
    void create_WhenKeycloakUserIdIsNull_ShouldSaveDirectly() {
      // Given
      User inputUser = User.builder().username("nouserid").build();
      User savedUser = User.builder().username("nouserid").build();

      when(userRepository.save(inputUser)).thenReturn(savedUser);

      // When
      User result = userService.create(inputUser);

      // Then
      assertThat(result).isNotNull().isEqualTo(savedUser);
      verify(userRepository).save(inputUser);
      verify(userRepository, org.mockito.Mockito.never())
          .findByKeycloakUserId(org.mockito.ArgumentMatchers.any());
    }
  }

  @Nested
  @DisplayName("update(User user)")
  class UpdateTest {

    @Test
    @DisplayName("should save and return updated User when user has valid id")
    void update_WhenValidUserWithId_ShouldSaveAndReturnUser() {
      // Given
      UUID userId = UUID.randomUUID();
      UUID keycloakUserId = UUID.randomUUID();
      User user =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("johndoe")
              .email("john@example.com")
              .firstName("UpdatedFirst")
              .lastName("UpdatedLast")
              .build();
      user.setId(userId);

      when(userRepository.existsById(userId)).thenReturn(true);
      when(userRepository.save(user)).thenReturn(user);

      // When
      User result = userService.update(user);

      // Then
      assertThat(result).isNotNull().isEqualTo(user);
      verify(userRepository).existsById(userId);
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("should throw InvalidInputException when user has null id")
    void update_WhenUserHasNoId_ShouldThrowInvalidInputException() {
      // Given
      User user = User.builder().username("johndoe").build();

      // When & Then
      assertThatThrownBy(() -> userService.update(user))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Tried to update user with invalid id");
    }

    @Test
    @DisplayName("should throw InvalidInputException when user does not exist in repository")
    void update_WhenUserDoesNotExistInRepo_ShouldThrowInvalidInputException() {
      // Given
      UUID userId = UUID.randomUUID();
      User user = User.builder().username("johndoe").build();
      user.setId(userId);

      when(userRepository.existsById(userId)).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> userService.update(user))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Tried to update user with invalid id");
    }

    @Test
    @DisplayName("should throw NullPointerException when user is null")
    void update_WhenUserIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> userService.update(null)).isInstanceOf(NullPointerException.class);
    }
  }
}
