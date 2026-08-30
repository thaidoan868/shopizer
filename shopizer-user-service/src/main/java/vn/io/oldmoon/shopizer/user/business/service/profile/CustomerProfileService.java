package vn.io.oldmoon.shopizer.user.business.service.profile;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
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
  private final UserPopulator userPopulator;


  @NonNull
  /**
   * Fetches a CustomerProfile by the associated Keycloak user ID.
   *
   * @param keycloakUserId The UUID of the Keycloak user.
   * @return An Optional containing the CustomerProfile if found, or an empty Optional if not found.
   */
  public Optional<CustomerProfile> get(UUID keycloakUserId) {
    Optional<CustomerProfileQueryDto> existingProfile =
        customerProfileRepository.findByKeycloakUserId(keycloakUserId);
    if (existingProfile.isPresent()) {
      log.info("Fetching Customer Profile for keycloakUserId={}", keycloakUserId);
      return customerProfileRepository.findById(existingProfile.get().id());
    }
    log.info("Customer Profile Not Found for keycloakUserId={}", keycloakUserId);
    return Optional.empty();
  }

  @Transactional
  public CustomerProfile create(CustomerProfile customerProfile) {
    Optional<CustomerProfile> existing = get(customerProfile.getUser().getKeycloakUserId());

    if (existing.isPresent()) {
      log.info(
          "CustomerProfile with keycloakUserId={} already exists. Skipping insertion for idempotency.",
          customerProfile.getUser().getKeycloakUserId());
      return existing.get();
    }

    log.info(
        "Persisting a customer profile with userKeycloakUserId={}",
        customerProfile.getUser().getKeycloakUserId());
    CustomerProfile profile = customerProfileRepository.save(customerProfile);
    return profile;
  }

  @Transactional
  public CustomerProfile create(User notSavedUser) {
    User savedUser = userService.create(notSavedUser);
    CustomerProfile profile = CustomerProfile.builder().user(savedUser).build();
    return this.create(profile);
  }
}
