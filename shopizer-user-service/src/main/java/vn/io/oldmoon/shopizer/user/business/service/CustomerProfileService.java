package vn.io.oldmoon.shopizer.user.business.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerProfileService implements ProfileService {
  private final CustomerProfileRepository customerProfileRepository;

  @NonNull
  public User get(UUID keycloakUserId) {
    User user =
        customerProfileRepository
            .findByKeycloakUserId(keycloakUserId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "CustomerProfile with", "keycloakUserId: " + keycloakUserId.toString()));
    log.info("Fetched Customer profile: userId={}", keycloakUserId);
    return user;
  }

  @Transactional
  public CustomerProfile create(CustomerProfile customerProfile) {
    CustomerProfile profile = customerProfileRepository.save(customerProfile);
    log.info("Created a customer profile with userKeycloakUserId={}", profile.getKeycloakUserId());
    return profile;
  }
}
