package vn.io.oldmoon.shopizer.user.business.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
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
   * @throws ResourceNotFoundException if no user exists with that ID
   */
  @NonNull
  public User get(UUID keycloakUserId) {
    User user =
        userRepository
            .findByKeycloakUserId(keycloakUserId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User with", "keycloakUserId: " + keycloakUserId.toString()));
    log.info("Fetched Customer profile: userId={}", keycloakUserId);
    return user;
  }

  @Transactional
  public User create(User user) {
    User newUser = userRepository.save(user);
    log.info("Created a user with userKeycloakUserId={}", newUser.getKeycloakUserId());
    return newUser;
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
}
