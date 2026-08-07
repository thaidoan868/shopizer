// package vn.io.oldmoon.shopizer.user.business.service;
//
// import java.util.UUID;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.lang.NonNull;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
// import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
// import vn.io.oldmoon.shopizer.user.infra.model.User;
// import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;
//
// @Service
// @RequiredArgsConstructor
// @Transactional(readOnly = true)
// @Slf4j
// public class CustomerService {
//  private final CustomerProfileRepository profileRepo;
//
//  /**
//   * @throws ResourceNotFoundException if no user exists with that ID
//   */
//  @NonNull
//  public User get(UUID userId) {
//    User profile =
//        profileRepo
//            .findByUserId(userId)
//            .orElseThrow(
//                () ->
//                    new ResourceNotFoundException(
//                        "CustomerProfile", "userId: " + userId.toString()));
//    log.info("Fetched Customer profile: userId={}", userId);
//    return profile;
//  }
//
//  @Transactional
//  public User createProfile(User profile) {
//    log.info("Attempting to persist profile for user={}", profile.getUserId());
//    User createdProfile = profileRepo.save(profile);
//    return createdProfile;
//  }
//
//  /**
//   * @throws InvalidInputException if tried to update a profile without id
//   */
//  @Transactional
//  public User updateProfile(User profile) {
//    if (profile.getId() == null) {
//      throw new InvalidInputException("Tried to update profile without id");
//    }
//    log.info("Attempting to update profile for user={}", profile.getUserId());
//    User updatedProfile = profileRepo.save(profile);
//    return updatedProfile;
//  }
// }
