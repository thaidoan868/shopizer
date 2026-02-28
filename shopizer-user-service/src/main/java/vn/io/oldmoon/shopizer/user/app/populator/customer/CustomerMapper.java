package vn.io.oldmoon.shopizer.user.app.populator.customer;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
  // TO MODELS
  UserRepresentation toUserRep(PersistableCustomer persistableCustomer);

  CustomerProfile toProfile(PersistableCustomer persistableCustomer);

  // TO RESPONSE
  @Mapping(target = "id", ignore = true)
  CreatedUserResponse toCreatedUser(UserRepresentation userRep);

  @Mapping(target = "avatarMeta", ignore = true)
  CustomerProfileResponse toProfileResponse(CustomerProfile profile);

  @Mapping(target = "avatarMeta", ignore = true)
  PublicCustomerProfileResponse toPublicProfileResponse(CustomerProfile profile);
}
