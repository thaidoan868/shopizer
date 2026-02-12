package vn.io.oldmoon.shopizer.user.app.populator.user;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    CreatedUser toCreatedUser(UserRepresentation user);
}