package vn.io.oldmoon.shopizer.user.business.service.profile;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerProfileService implements ProfileService {
  private final CustomerProfileRepository customerProfileRepository;
  private final UserService userService;

  @Override
  public Profile update() {
    return null;
  }

  @Override
  public Role getSupportedRole() {
    return Role.CUSTOMER;
  }

  @NonNull
  public CustomerProfile get(UUID keycloakUserId) {
    CustomerProfile profile =
        customerProfileRepository
            .findByKeycloakUserId(keycloakUserId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "CustomerProfile with", "userId: " + keycloakUserId.toString()));
    log.info("Fetched Customer profile: userId={}", keycloakUserId);
    return profile;
  }

  @Transactional
  public CustomerProfile create(CustomerProfile customerProfile) {
    CustomerProfile profile = customerProfileRepository.save(customerProfile);
    log.info("Created a customer profile with userKeycloakUserId={}", profile.getKeycloakUserId());
    return profile;
  }

  /*
  Create a new profile for a user that hasn't been saved
   */
  @Transactional
  public CustomerProfile create(User notSavedUser) {
    User savedUser = userService.create(notSavedUser);
    CustomerProfile profile =
        CustomerProfile.builder()
            .user(savedUser)
            .keycloakUserId(savedUser.getKeycloakUserId())
            .build();
    return customerProfileRepository.save(profile);
  }
}
