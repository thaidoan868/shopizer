package vn.io.oldmoon.shopizer.user.app.transfer.populator.customer;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.*;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.UpdateCustomerProfileRequest;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
  // TO MODELS
  UserRepresentation toUserRep(PersistableCustomer persistableCustomer);

  CustomerProfile toProfile(PersistableCustomer persistableCustomer);

  // Update
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  CustomerProfile patchUpdate(
      UpdateCustomerProfileRequest request, @MappingTarget CustomerProfile profile);

  // TO RESPONSE
  @Mapping(target = "id", ignore = true)
  CreatedUserResponse toCreatedUser(UserRepresentation userRep);

  @Mapping(target = "avatarMeta", ignore = true)
  CustomerProfileResponse toProfileResponse(CustomerProfile profile);

  @Mapping(target = "avatarMeta", ignore = true)
  PublicCustomerProfileResponse toPublicProfileResponse(CustomerProfile profile);
}
