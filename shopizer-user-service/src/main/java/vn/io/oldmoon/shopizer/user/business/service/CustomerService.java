package vn.io.oldmoon.shopizer.user.business.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.user.business.exception.BusinessException;
import vn.io.oldmoon.shopizer.user.business.exception.ErrorCode;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerService {
  private final CustomerProfileRepository profileRepo;

  /**
   * @throws BusinessException if no user exists with that ID
   */
  @NonNull
  public CustomerProfile get(UUID userId) {
    CustomerProfile profile =
        profileRepo
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.NOT_FOUND, "Not found profile with userId=" + userId));
    log.info("Fetched Customer profile: userId={}", userId);
    return profile;
  }

  public CustomerProfile createProfile(CustomerProfile profile) {
    CustomerProfile createdProfile = profileRepo.save(profile);
    log.info(
        "Profile Created: id={}, userId={}", createdProfile.getId(), createdProfile.getUserId());
    return createdProfile;
  }

  /**
   * @throws RuntimeException if tried to update a profile without id
   */
  public CustomerProfile updateProfile(CustomerProfile profile) {
    if (profile.getId() == null) {
      throw new RuntimeException("Tried to update profile without id");
    }
    CustomerProfile updatedProfile = profileRepo.save(profile);

    log.info("Updated profile: userId={}", updatedProfile.getUserId());
    return updatedProfile;
  }
}
