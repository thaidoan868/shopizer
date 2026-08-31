package vn.io.oldmoon.shopizer.user.business.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
  private final UserRepository userRepository;

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
}

  /**
   * @throws InvalidInputException if tried to update a profile without id
   */
  //  @Transactional
  //  public User updateProfile(User profile) {
  //    if (profile.getId() == null) {
  //      throw new InvalidInputException("Tried to update profile without id");
  //    }
  //    log.info("Attempting to update profile for user={}", profile.getUserId());
  //    User updatedProfile = profileRepo.save(profile);
  //    return updatedProfile;
  //  }
