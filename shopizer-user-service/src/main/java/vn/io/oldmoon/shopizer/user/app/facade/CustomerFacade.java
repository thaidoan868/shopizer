package vn.io.oldmoon.shopizer.user.app.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.UpdateCustomerProfileRequest;
import vn.io.oldmoon.shopizer.user.app.transfer.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.business.service.CustomerService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@Service
@RequiredArgsConstructor
public class CustomerFacade {
  private final CustomerPopulator customerPopulator;
  private final CustomerService customerService;

  public CustomerProfileResponse getProfile(UUID userId) {
    // get profile
    CustomerProfile profile = customerService.get(userId);

    return customerPopulator.toProfileResponse(profile);
  }

  public PublicCustomerProfileResponse getPublicProfile(UUID userId) {
    // get profile
    CustomerProfile profile = customerService.get(userId);

    return customerPopulator.toPublicProfileResponse(profile);
  }

  public CustomerProfileResponse updateProfile(UUID userId, UpdateCustomerProfileRequest request) {
    // get the current user's profile
    CustomerProfile profile = customerService.get(userId);

    // update the profile and save the updated profile
    customerPopulator.patchUpdate(request, profile);
    CustomerProfile newProfile = customerService.updateProfile(profile);

    // convert to response
    return customerPopulator.toProfileResponse(newProfile);
  }
}
