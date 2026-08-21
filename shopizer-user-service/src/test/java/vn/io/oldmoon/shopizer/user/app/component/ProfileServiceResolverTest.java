package vn.io.oldmoon.shopizer.user.app.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import vn.io.oldmoon.shopizer.user.business.service.profile.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.business.service.profile.ProfileService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

class ProfileServiceResolverTest {

  @Test
  void shouldReturnMatchingServiceForRole() {
    CustomerProfileService customerProfileService = mock(CustomerProfileService.class);
    when(customerProfileService.getSupportedRole()).thenReturn(Role.CUSTOMER);

    ProfileServiceResolver resolver = new ProfileServiceResolver(List.of(customerProfileService));

    assertThat(resolver.getService(Role.CUSTOMER)).isEqualTo(customerProfileService);
  }

  @Test
  void shouldThrowExceptionWhenRoleNotFound() {
    CustomerProfileService customerProfileService = mock(CustomerProfileService.class);
    when(customerProfileService.getSupportedRole()).thenReturn(Role.CUSTOMER);
    ProfileServiceResolver resolver = new ProfileServiceResolver(List.of(customerProfileService));

    assertThatThrownBy(() -> resolver.getService(Role.SUPER_ADMIN))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldFailOnInitializationWhenDuplicateRolesExist() {
    ProfileService service1 = mock(ProfileService.class);
    ProfileService service2 = mock(ProfileService.class);
    when(service1.getSupportedRole()).thenReturn(Role.CUSTOMER);
    when(service2.getSupportedRole()).thenReturn(Role.CUSTOMER);

    assertThatThrownBy(() -> new ProfileServiceResolver(List.of(service1, service2)))
        .isInstanceOf(IllegalStateException.class);
  }
}
