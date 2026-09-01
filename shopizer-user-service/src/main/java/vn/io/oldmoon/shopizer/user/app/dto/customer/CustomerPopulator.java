package vn.io.oldmoon.shopizer.user.app.dto.customer;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPopulator {

  private final CustomerMapper customerMapper;
  private final UserPopulator userPopulator;

  /**
   * Converts a User and CustomerProfile entity into a CustomerProfileDto.
   *
   * @param user the User entity
   * @param customerProfile the CustomerProfile entity
   * @return the corresponding CustomerProfileDto
   */
  public CustomerProfileDto toCustomerProfileDto(User user, CustomerProfile customerProfile) {
    Objects.requireNonNull(customerProfile);
    Objects.requireNonNull(user);

    CustomerProfileDto profileDto = customerMapper.toCustomerProfileDto(user, customerProfile);
    if (user.getAvatarMeta() != null) {
      profileDto.setAvatarMeta(userPopulator.toAvatarDto(user.getAvatarMeta()));
    }
    log.info(
        "Converted User and CustomerProfile to CustomerProfileDto for keycloakUserId={}",
        user.getKeycloakUserId());
    return profileDto;
  }

  /**
   * Updates target User and CustomerProfile entities in-place from an UpdateCustomerDto.
   *
   * @param user the target User entity
   * @param customerProfile the target CustomerProfile entity
   * @param updateCustomerDto the incoming update DTO
   */
  public void update(
      User user, CustomerProfile customerProfile, UpdateCustomerDto updateCustomerDto) {
    Objects.requireNonNull(user);
    Objects.requireNonNull(customerProfile);
    Objects.requireNonNull(updateCustomerDto);

    customerMapper.updateUserFromDto(updateCustomerDto, user);
    customerMapper.updateCustomerProfileFromDto(updateCustomerDto, customerProfile);
    log.info(
        "Updated User and CustomerProfile entities in-place for keycloakUserId={}",
        user.getKeycloakUserId());
  }
}
