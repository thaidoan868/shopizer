package vn.io.oldmoon.shopizer.user.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.util.ImageUtil;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final FileMetaService fileMetaService;

  @Value("${minio.bucket:avatar-public}")
  private String avatarBucket;

  /**
   * Fetches a user by their Keycloak user ID.
   *
   * @throws ResourceNotFoundException if user not found
   */
  public User get(UUID keycloakUserId) {
    User user =
        userRepository
            .findByKeycloakUserId(keycloakUserId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User", "userId=%s".formatted(keycloakUserId.toString())));

    log.debug("Fetched User: userId={}", keycloakUserId);
    return user;
  }

  /**
   * Creates a new user in the database. If a user with the same Keycloak user ID already exists, it
   * returns the existing user for idempotency.
   */
  @Transactional
  public User create(User user) {
    Objects.requireNonNull(user);

    if (user.getKeycloakUserId() != null) {
      Optional<User> existing = userRepository.findByKeycloakUserId(user.getKeycloakUserId());
      if (existing.isPresent()) {
        log.info(
            "User with keycloakUserId={} already exists. Skipping insertion for idempotency.",
            user.getKeycloakUserId());
        return existing.get();
      }
    }
    User newUser = userRepository.save(user);
    log.info("Persisting user entity userKeycloakUserId={}", newUser.getKeycloakUserId());
    return newUser;
  }

  /**
   * Updates an existing user in the database.
   *
   * @throws InvalidInputException if tried to update a user with invalid id
   */
  @Transactional
  public User update(User user) {
    Objects.requireNonNull(user);
    if (user.getId() == null || !userRepository.existsById(user.getId())) {
      throw new InvalidInputException("Tried to update user with invalid id");
    }
    log.info(
        "Updating user entity userKeycloakUserId={}",
        user.getKeycloakUserId() != null ? user.getKeycloakUserId() : "null");
    return userRepository.save(user);
  }

  @Transactional
  public User updateAvatar(UUID keycloakUserId, MultipartFile file) {
    Objects.requireNonNull(keycloakUserId);
    Objects.requireNonNull(file);
    ImageUtil.validateContent(file);

    String uniqueId = UUID.randomUUID().toString();
    String originalObjectName =
        "avatar-original-" + keycloakUserId + "-" + uniqueId + "-" + file.getOriginalFilename();
    String mediumObjectName = "avatar-medium-" + keycloakUserId + "-" + uniqueId + ".jpg";
    String thumbnailObjectName = "avatar-thumbnail-" + keycloakUserId + "-" + uniqueId + ".jpg";

    String contentType = "image/jpg";
    FileMeta originalFileMetaMeta =
        FileMeta.builder()
            .bucket(avatarBucket)
            .objectName(originalObjectName)
            .contentType("image")
            .visibility(Visibility.PUBLIC)
            .build();

    FileMeta mediumFileMetaMeta =
        FileMeta.builder()
            .bucket(avatarBucket)
            .objectName(mediumObjectName)
            .contentType(contentType)
            .visibility(Visibility.PUBLIC)
            .build();

    FileMeta thumbnailFileMetaMeta =
        FileMeta.builder()
            .bucket(avatarBucket)
            .objectName(thumbnailObjectName)
            .contentType(contentType)
            .visibility(Visibility.PUBLIC)
            .build();

    try (InputStream fileInputStream = file.getInputStream()) {
      InputStream mediumImagePayload = ImageUtil.resizeImageToStream(fileInputStream, 300, 300);
      InputStream thumbnailImagePayload = ImageUtil.resizeImageToStream(fileInputStream, 150, 150);

      fileMetaService.save(fileInputStream, originalFileMetaMeta);
      fileMetaService.save(mediumImagePayload, mediumFileMetaMeta);
      fileMetaService.save(thumbnailImagePayload, thumbnailFileMetaMeta);

    } catch (IOException e) {
      log.error("Failed to resize the avatar image for keycloakUserId={}", keycloakUserId, e);
      throw new InvalidInputException("Failed to process avatar image: " + e.getMessage());
    }
    User user = get(keycloakUserId);
    AvatarMeta avatarMeta =
        new AvatarMeta(avatarBucket, originalObjectName, mediumObjectName, thumbnailObjectName);
    AvatarMeta oldAvatarMeta = user.getAvatarMeta();
    user.setAvatarMeta(avatarMeta);
    User updatedUser = userRepository.save(user);

    try {
      if (oldAvatarMeta != null && oldAvatarMeta.bucket() != null) {
        if (oldAvatarMeta.originalObjectName() != null) {
          fileMetaService.delete(oldAvatarMeta.bucket(), oldAvatarMeta.originalObjectName());
        }
        if (oldAvatarMeta.mediumObjectName() != null) {
          fileMetaService.delete(oldAvatarMeta.bucket(), oldAvatarMeta.mediumObjectName());
        }
        if (oldAvatarMeta.thumbnailObjectName() != null) {
          fileMetaService.delete(oldAvatarMeta.bucket(), oldAvatarMeta.thumbnailObjectName());
        }
      }
    } catch (ResourceNotFoundException e) {
      log.warn(
          "Trying to delete old avatar files but they do not exist. skipping deletion for idempotency. keycloakUserId={}",
          keycloakUserId);
    }
    return updatedUser;
  }
}
