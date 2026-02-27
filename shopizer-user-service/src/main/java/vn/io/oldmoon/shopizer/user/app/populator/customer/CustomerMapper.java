package vn.io.oldmoon.shopizer.user.app.populator.customer;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    UserRepresentation toUserRep(PersistableCustomer persistableCustomer);

    CustomerProfile toProfile(PersistableCustomer persistableCustomer);

    @Mapping(target = "id", ignore = true)
    CreatedUserResponse toCreatedUser(UserRepresentation userRep);
}

