package vn.io.oldmoon.shopizer.user.app.populator.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPopulator {
    private final CustomerMapper customerMapper;

    public UserRepresentation toUserRep(PersistableCustomer persistableCustomer) {
        UserRepresentation userRep = customerMapper.toUserRep(persistableCustomer);
        userRep.setEnabled(true);
        userRep.setEmailVerified(false);
        log.debug(
                "Converted PersistableCustomer to UserRepresentation, username={}",
                persistableCustomer.getUsername()
        );
        return userRep;
    }

    public CustomerProfile toProfile(PersistableCustomer persistableCustomer) {
        var result = customerMapper.toProfile(persistableCustomer);
        log.debug(
                "Converted PersistableCustomer to CustomerProfile, username={}",
                persistableCustomer.getUsername()
        );
        return result;
    }

    public CreatedUser toCreatedUser(UserRepresentation userRep) {
        CreatedUser createdUser = customerMapper.toCreatedUser(userRep);
        createdUser.setId(UUID.fromString(userRep.getId()));
        log.debug(
                "Converted UserRepresentation to CreatedUser, userid={}",
                userRep.getId()
        );
        return createdUser;
    }


}
