package vn.io.oldmoon.shopizer.user.app.component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.user.business.service.profile.ProfileService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@Component
public class ProfileServiceResolver {

  private final Map<Role, ProfileService> serviceMap;

  public ProfileServiceResolver(List<ProfileService> services) {
    this.serviceMap =
        services.stream()
            .collect(Collectors.toMap(ProfileService::getSupportedRole, Function.identity()));
  }

  public ProfileService getService(Role role) {
    ProfileService service = serviceMap.get(role);
    if (service == null) {
      throw new IllegalArgumentException("No profile service found for role: " + role);
    }
    return service;
  }
}
