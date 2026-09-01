package vn.io.oldmoon.shopizer.user.app.dto.employee;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeePopulator {

  private final EmployeeMapper employeeMapper;
  private final UserPopulator userPopulator;

  /**
   * Converts a User and EmployeeProfile entity into an EmployeeProfileDto.
   *
   * @param user the User entity
   * @param employeeProfile the EmployeeProfile entity
   * @return the corresponding EmployeeProfileDto
   */
  public EmployeeProfileDto toEmployeeProfileDto(User user, EmployeeProfile employeeProfile) {
    Objects.requireNonNull(employeeProfile);
    Objects.requireNonNull(user);

    EmployeeProfileDto profileDto = employeeMapper.toEmployeeProfileDto(user, employeeProfile);
    if (user.getAvatarMeta() != null) {
      profileDto.setAvatarMeta(userPopulator.toAvatarDto(user.getAvatarMeta()));
    }
    log.info(
        "Converted User and EmployeeProfile to EmployeeProfileDto for keycloakUserId={}",
        user.getKeycloakUserId());
    return profileDto;
  }
}
