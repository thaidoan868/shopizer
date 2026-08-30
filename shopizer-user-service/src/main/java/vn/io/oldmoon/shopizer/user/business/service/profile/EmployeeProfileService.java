package vn.io.oldmoon.shopizer.user.business.service.profile;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeProfileService {
  private final EmployeeProfileRepository employeeProfileRepository;

  @NonNull
  public Optional<EmployeeProfile> get(UUID keycloakUserId) {
    Optional<EmployeeProfileQueryDto> existingProfile =
        employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
    if (existingProfile.isPresent()) {
      log.info("Fetching Employee Profile for keycloakUserId={}", keycloakUserId);
      return employeeProfileRepository.findById(existingProfile.get().id());
    }
    log.info("Employee Profile Not Found for keycloakUserId={}", keycloakUserId);
    return Optional.empty();
  }

  public boolean exists(UUID keycloakUserId) {
    return employeeProfileRepository.findByKeycloakUserId(keycloakUserId).isPresent();
  }

  @Transactional
  public EmployeeProfile create(EmployeeProfile employeeProfile) {
    Optional<EmployeeProfile> existing = get(employeeProfile.getUser().getKeycloakUserId());

    if (existing.isPresent()) {
      log.info(
          "Employee profile already exists for userId: {}. Skipping creation.",
          employeeProfile.getUser().getKeycloakUserId());
      return existing.get();
    }

    log.info(
        "Persisting an employee profile with userKeycloakUserId={}",
        employeeProfile.getUser().getKeycloakUserId());
    return employeeProfileRepository.save(employeeProfile);
  }
}
