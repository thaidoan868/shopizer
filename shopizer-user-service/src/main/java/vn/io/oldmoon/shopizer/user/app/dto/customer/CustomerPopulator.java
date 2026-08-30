package vn.io.oldmoon.shopizer.user.app.dto.customer;

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

  private final UserPopulator userPopulator;

  public CustomerProfileDto toCustomerProfileDto(User user, CustomerProfile customerProfile) {
    CustomerProfileDto profileDto = customerMapper.toCustomerProfileDto(user, customerProfile);
    if (user != null && user.getAvatarMeta() != null) {
      profileDto.setAvatarMeta(userPopulator.toAvatarDto(user.getAvatarMeta()));
    }
    return profileDto;
  }
}
