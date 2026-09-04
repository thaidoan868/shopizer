package vn.io.oldmoon.shopizer.user.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private FileMetaService fileMetaService;

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

  @Nested
  @DisplayName("verifyEmail(UUID keycloakUserId, String verifiedEmail)")
  class VerifyEmailTest {

    @Test
    @DisplayName("should update verified to true when user exists and email matches")
    void verifyEmail_WhenUserExistsAndEmailMatches_ShouldUpdateVerified() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      User user =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("johndoe")
              .email("user@example.com")
              .verified(false)
              .build();

      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(user));
      when(userRepository.save(user)).thenReturn(user);

      // When
      User result = userService.verifyEmail(keycloakUserId, "user@example.com");

      // Then
      assertThat(result.getVerified()).isTrue();
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName(
        "should throw ResourceConflictException when verified email does not match user email")
    void verifyEmail_WhenEmailMismatch_ShouldThrowResourceConflictException() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      User user =
          User.builder()
              .keycloakUserId(keycloakUserId)
              .username("johndoe")
              .email("user@example.com")
              .verified(false)
              .build();

      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.of(user));

      // When & Then
      assertThatThrownBy(() -> userService.verifyEmail(keycloakUserId, "mismatch@example.com"))
          .isInstanceOf(vn.io.oldmoon.shopizer.common.core.exception.ResourceConflictException.class)
          .hasMessageContaining("does not match the existing email");
    }

    @Test
    @DisplayName("should throw InvalidInputException when verified email is blank")
    void verifyEmail_WhenEmailBlank_ShouldThrowInvalidInputException() {
      UUID keycloakUserId = UUID.randomUUID();
      assertThatThrownBy(() -> userService.verifyEmail(keycloakUserId, "   "))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Verified email must not be blank");
    }

    @Test
    @DisplayName("should throw NullPointerException when verifiedEmail is null")
    void verifyEmail_WhenVerifiedEmailNull_ShouldThrowNpe() {
      UUID keycloakUserId = UUID.randomUUID();
      assertThatThrownBy(() -> userService.verifyEmail(keycloakUserId, null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when keycloakUserId is null")
    void verifyEmail_WhenUserIdNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> userService.verifyEmail(null, "user@example.com"))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user does not exist")
    void verifyEmail_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
      // Given
      UUID keycloakUserId = UUID.randomUUID();
      when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.verifyEmail(keycloakUserId, "user@example.com"))
          .isInstanceOf(ResourceNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("updateAvatar(UUID userId, MultipartFile file)")
  class UpdateAvatarTest {

    @BeforeEach
    void setUp() {
      ReflectionTestUtils.setField(userService, "avatarBucket", "public-assets");
    }

    private byte[] createValidImageBytes() {
      try {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(50, 50, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        return baos.toByteArray();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Test
    @DisplayName("should save image variants and update user avatarMeta when input is valid")
    void updateAvatar_WhenValidInput_ShouldUpdateAvatarAndSaveUser() {
      // Given
      UUID userId = UUID.randomUUID();
      User user = User.builder().keycloakUserId(userId).username("avatarUser").build();
      byte[] imageBytes = createValidImageBytes();
      org.springframework.mock.web.MockMultipartFile file =
          new org.springframework.mock.web.MockMultipartFile(
              "file", "my-avatar.png", "image/png", imageBytes);

      when(userRepository.findByKeycloakUserId(userId)).thenReturn(Optional.of(user));
      when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      when(fileMetaService.save(
              org.mockito.ArgumentMatchers.any(java.io.InputStream.class),
              org.mockito.ArgumentMatchers.any(FileMeta.class)))
          .thenAnswer(invocation -> invocation.getArgument(1));

      // When
      User updatedUser = userService.updateAvatar(userId, file);

      // Then
      assertThat(updatedUser).isNotNull();
      assertThat(updatedUser.getAvatarMeta()).isNotNull();
      assertThat(updatedUser.getAvatarMeta().bucket()).isEqualTo("public-assets");
      assertThat(updatedUser.getAvatarMeta().originalObjectName()).contains(userId.toString());
      assertThat(updatedUser.getAvatarMeta().originalObjectName()).endsWith(".png");
      assertThat(updatedUser.getAvatarMeta().mediumObjectName()).contains("medium");
      assertThat(updatedUser.getAvatarMeta().thumbnailObjectName()).contains("thumbnail");

      verify(fileMetaService, org.mockito.Mockito.times(3))
          .save(
              org.mockito.ArgumentMatchers.any(java.io.InputStream.class),
              org.mockito.ArgumentMatchers.any(FileMeta.class));
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("should delete old avatar files when user already has an avatar")
    void updateAvatar_WhenOldAvatarExists_ShouldDeleteOldAvatarFiles() {
      // Given
      UUID userId = UUID.randomUUID();
      AvatarMeta oldAvatar =
          new AvatarMeta("public-assets", "old-orig.jpg", "old-med.jpg", "old-thumb.jpg");
      User user =
          User.builder()
              .keycloakUserId(userId)
              .username("avatarUser")
              .avatarMeta(oldAvatar)
              .build();
      byte[] imageBytes = createValidImageBytes();
      org.springframework.mock.web.MockMultipartFile file =
          new org.springframework.mock.web.MockMultipartFile(
              "file", "my-avatar.png", "image/png", imageBytes);

      when(userRepository.findByKeycloakUserId(userId)).thenReturn(Optional.of(user));
      when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      when(fileMetaService.save(
              org.mockito.ArgumentMatchers.any(java.io.InputStream.class),
              org.mockito.ArgumentMatchers.any(FileMeta.class)))
          .thenAnswer(invocation -> invocation.getArgument(1));

      // When
      User updatedUser = userService.updateAvatar(userId, file);

      // Then
      assertThat(updatedUser).isNotNull();
      verify(fileMetaService).delete("public-assets", "old-orig.jpg");
      verify(fileMetaService).delete("public-assets", "old-med.jpg");
      verify(fileMetaService).delete("public-assets", "old-thumb.jpg");
    }

    @Test
    @DisplayName("should throw NullPointerException when userId is null")
    void updateAvatar_WhenUserIdIsNull_ShouldThrowNpe() {
      byte[] imageBytes = createValidImageBytes();
      org.springframework.mock.web.MockMultipartFile file =
          new org.springframework.mock.web.MockMultipartFile(
              "file", "avatar.png", "image/png", imageBytes);

      assertThatThrownBy(() -> userService.updateAvatar(null, file))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when file is null")
    void updateAvatar_WhenFileIsNull_ShouldThrowNpe() {
      UUID userId = UUID.randomUUID();

      assertThatThrownBy(() -> userService.updateAvatar(userId, null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user is not found")
    void updateAvatar_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
      UUID userId = UUID.randomUUID();
      byte[] imageBytes = createValidImageBytes();
      org.springframework.mock.web.MockMultipartFile file =
          new org.springframework.mock.web.MockMultipartFile(
              "file", "avatar.png", "image/png", imageBytes);

      when(userRepository.findByKeycloakUserId(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.updateAvatar(userId, file))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should throw InvalidInputException when image content is empty")
    void updateAvatar_WhenImageIsEmpty_ShouldThrowInvalidInputException() {
      UUID userId = UUID.randomUUID();
      org.springframework.mock.web.MockMultipartFile emptyFile =
          new org.springframework.mock.web.MockMultipartFile(
              "file", "avatar.png", "image/png", new byte[0]);

      assertThatThrownBy(() -> userService.updateAvatar(userId, emptyFile))
          .isInstanceOf(InvalidInputException.class);
    }
  }
}
