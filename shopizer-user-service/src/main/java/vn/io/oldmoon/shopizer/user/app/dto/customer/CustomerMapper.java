// package vn.io.oldmoon.shopizer.user.app.transfer.populator.customer;
//
// import org.keycloak.representations.idm.UserRepresentation;
// import org.mapstruct.*;
// import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.CreatedUserResponse;
// import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.PersistableCustomer;
// import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.CustomerProfileResponse;
// import
// vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.PublicCustomerProfileResponse;
// import
// vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.UpdateCustomerProfileRequest;
// import vn.io.oldmoon.shopizer.user.infra.model.User;
//
// @Mapper(componentModel = "spring")
// public interface CustomerMapper {
//  // TO MODELS
//  UserRepresentation toUserRep(PersistableCustomer persistableCustomer);
//
//  User toProfile(PersistableCustomer persistableCustomer);
//
//  // Update
//  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//  User patchUpdate(UpdateCustomerProfileRequest request, @MappingTarget User profile);
//
//  // TO RESPONSE
//  @Mapping(target = "id", ignore = true)
//  CreatedUserResponse toCreatedUser(UserRepresentation userRep);
//
//  @Mapping(target = "avatarMeta", ignore = true)
//  CustomerProfileResponse toProfileResponse(User profile);
//
//  @Mapping(target = "avatarMeta", ignore = true)
//  PublicCustomerProfileResponse toPublicProfileResponse(User profile);
// }
