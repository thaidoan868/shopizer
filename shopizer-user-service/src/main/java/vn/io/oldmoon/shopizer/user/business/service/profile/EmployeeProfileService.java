package vn.io.oldmoon.shopizer.user.business.service.profile;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeProfileService {
  private final EmployeeProfileRepository employeeProfileRepository;
  private final UserService userService;

  @NonNull
  public EmployeeProfile get(UUID keycloakUserId) {
    Optional<EmployeeProfileQueryDto> existingProfile =
        employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
    if (existingProfile.isPresent()) {
      log.info("Fetching Employee Profile for keycloakUserId={}", keycloakUserId);
      return employeeProfileRepository
          .findById(existingProfile.get().id())
          .orElseThrow(
              () ->
                  new ResourceNotFoundException(
                      "EmployeeProfile", "userId=%s".formatted(keycloakUserId.toString())));
    } else {
      throw new ResourceNotFoundException(
          "EmployeeProfile", "userId=%s".formatted(keycloakUserId.toString()));
    }
  }

  public boolean exists(UUID keycloakUserId) {
    return employeeProfileRepository.findByKeycloakUserId(keycloakUserId).isPresent();
  }

  /**
   * Creates a new employee profile in the database. If a profile with the same Keycloak user ID
   * already exists, it returns the existing profile for idempotency.
   *
   * @param employeeProfile the employee profile to create
   */
  @Transactional
  public EmployeeProfile create(EmployeeProfile employeeProfile) {
    try {
      EmployeeProfile existingProfile = get(employeeProfile.getUser().getKeycloakUserId());
      log.info(
          "EmployeeProfile with keycloakUserId={} already exists. Skipping insertion for idempotency.",
          employeeProfile.getUser().getKeycloakUserId());
      return existingProfile;
    } catch (ResourceNotFoundException e) {
      log.info(
          "Persisting an employee profile with userKeycloakUserId={}",
          employeeProfile.getUser().getKeycloakUserId());
      return employeeProfileRepository.save(employeeProfile);
    }
  }

  /**
   * Creates a new User and an initial EmployeeProfile in a single transaction.
   *
   * @return the created EmployeeProfile entity
   */
  @Transactional
  public EmployeeProfile create(User user) {
    Objects.requireNonNull(user);

    User savedUser = userService.create(user);
    EmployeeProfile profile = EmployeeProfile.builder().user(savedUser).build();
    profile.setCreatedBy(savedUser.getCreatedBy());
    return this.create(profile);
  }

  /**
   * Updates an existing employee profile in the database.
   *
   * @param profile the employee profile to update
   * @return the updated EmployeeProfile entity
   */
  @Transactional
  public EmployeeProfile update(EmployeeProfile profile) {
    Objects.requireNonNull(profile);
    if (profile.getId() == null || !employeeProfileRepository.existsById(profile.getId())) {
      throw new InvalidInputException("Tried to update employee profile with invalid id");
    }
    log.info(
        "Updating employee profile for keycloakUserId={}",
        profile.getUser() != null ? profile.getUser().getKeycloakUserId() : "null");
    return employeeProfileRepository.save(profile);
  }

  /**
   * Updates existing User and EmployeeProfile entities in a single transaction.
   *
   * @param user the existing User entity
   * @param profile the existing EmployeeProfile entity
   * @return the updated EmployeeProfile entity
   */
  @Transactional
  public EmployeeProfile update(User user, EmployeeProfile profile) {
    Objects.requireNonNull(user);
    Objects.requireNonNull(profile);
    User updatedUser = userService.update(user);
    profile.setUser(updatedUser);
    return this.update(profile);
  }
}

