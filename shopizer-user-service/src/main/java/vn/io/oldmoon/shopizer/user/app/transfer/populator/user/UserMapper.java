package vn.io.oldmoon.shopizer.user.app.transfer.populator.user;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.CreatedUserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "id", ignore = true)
  CreatedUserResponse toCreatedUser(UserRepresentation user);
}
