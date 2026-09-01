package vn.io.oldmoon.shopizer.user.business.service.profile;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerProfileService {
  private final CustomerProfileRepository customerProfileRepository;
  private final UserService userService;

  /**
   * Fetches a customer profile by the associated Keycloak user ID.
   *
   * @param keycloakUserId the Keycloak user ID
   * @return the CustomerProfile entity
   * @throws ResourceNotFoundException if no profile is found for the given Keycloak user ID
   */
  public CustomerProfile get(UUID keycloakUserId) {
    Optional<CustomerProfileQueryDto> existingProfile =
        customerProfileRepository.findByKeycloakUserId(keycloakUserId);
    if (existingProfile.isPresent()) {
      log.info("Fetching Customer Profile for keycloakUserId={}", keycloakUserId);
      return customerProfileRepository
          .findById(existingProfile.get().id())
          .orElseThrow(
              () ->
                  new ResourceNotFoundException(
                      "Profile", "userId=%s".formatted(keycloakUserId.toString())));
    } else {
      throw new ResourceNotFoundException(
          "Profile", "userId=%s".formatted(keycloakUserId.toString()));
    }
  }

  /**
   * Creates a new customer profile in the database. If a profile with the same Keycloak user ID
   * already exists, it returns the existing profile for idempotency.
   *
   * @param customerProfile the customer profile to create
   */
  @Transactional
  public CustomerProfile create(CustomerProfile customerProfile) {
    CustomerProfile existingProfile;
    CustomerProfile profile;
    try {
      existingProfile = get(customerProfile.getUser().getKeycloakUserId());
      log.info(
          "CustomerProfile with keycloakUserId={} already exists. Skipping insertion for idempotency.",
          customerProfile.getUser().getKeycloakUserId());
      return existingProfile;
    } catch (ResourceNotFoundException e) {
      log.info(
          "Persisting a customer profile with userKeycloakUserId={}",
          customerProfile.getUser().getKeycloakUserId());
      profile = customerProfileRepository.save(customerProfile);
      return profile;
    }
  }

  @Transactional
  public CustomerProfile create(User notSavedUser) {
    Objects.requireNonNull(notSavedUser);

    User savedUser = userService.create(notSavedUser);
    CustomerProfile profile = CustomerProfile.builder().user(savedUser).build();
    return this.create(profile);
  }

  /**
   * Updates an existing customer profile in the database.
   *
   * @param profile the customer profile to update
   * @return the updated CustomerProfile entity
   */
  @Transactional
  public CustomerProfile update(CustomerProfile profile) {
    Objects.requireNonNull(profile);
    if (profile.getId() == null || !customerProfileRepository.existsById(profile.getId())) {
      throw new InvalidInputException("Tried to update customer profile with invalid id");
    }
    log.info(
        "Updating customer profile for keycloakUserId={}",
        profile.getUser() != null ? profile.getUser().getKeycloakUserId() : "null");
    return customerProfileRepository.save(profile);
  }

  /**
   * Updates existing User and CustomerProfile entities in a single transaction.
   *
   * @param user the existing User entity
   * @param profile the existing CustomerProfile entity
   * @return the updated CustomerProfile entity
   */
  @Transactional
  public CustomerProfile update(User user, CustomerProfile profile) {
    Objects.requireNonNull(user);
    Objects.requireNonNull(profile);
    User updatedUser = userService.update(user);
    profile.setUser(updatedUser);
    return this.update(profile);
  }
}
