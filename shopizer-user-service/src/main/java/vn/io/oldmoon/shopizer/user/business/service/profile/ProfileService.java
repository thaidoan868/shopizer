package vn.io.oldmoon.shopizer.user.business.service.profile;

import org.springframework.context.annotation.Profile;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

public interface ProfileService {
  Profile update();

  Role getSupportedRole();
}
