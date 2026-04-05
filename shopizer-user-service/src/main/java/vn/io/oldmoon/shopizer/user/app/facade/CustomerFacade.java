package vn.io.oldmoon.shopizer.user.app.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
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

  private CustomerProfile getProfileOrThrow(UUID userId) {
    try {
      return customerService.get(userId);
    } catch (ResourceNotFoundException e) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Profile not found with userId: " + userId);
    }
  }

  public CustomerProfileResponse getProfile(UUID userId) {
    CustomerProfile profile = getProfileOrThrow(userId);
    return customerPopulator.toProfileResponse(profile);
  }

  public PublicCustomerProfileResponse getPublicProfile(UUID userId) {
    CustomerProfile profile = getProfileOrThrow(userId);
    return customerPopulator.toPublicProfileResponse(profile);
  }

  public CustomerProfileResponse updateProfile(UUID userId, UpdateCustomerProfileRequest request) {
    CustomerProfile profile = customerService.get(userId);

    customerPopulator.patchUpdate(request, profile);
    CustomerProfile newProfile = customerService.updateProfile(profile);

    return customerPopulator.toProfileResponse(newProfile);
  }
}
