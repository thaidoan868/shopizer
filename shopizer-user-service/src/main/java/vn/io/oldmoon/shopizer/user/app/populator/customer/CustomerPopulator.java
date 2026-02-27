package vn.io.oldmoon.shopizer.user.app.populator.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.infra.data.constant.KeycloakRequiredAction;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPopulator {
    private final CustomerMapper customerMapper;

    public UserRepresentation toUserRep(PersistableCustomer persistableCustomer) {
        List<String> requiredActions = List.of(KeycloakRequiredAction.VERIFY_EMAIL.toString());
        UserRepresentation userRep = customerMapper.toUserRep(persistableCustomer);
        userRep.setEnabled(true);
        userRep.setEmailVerified(false);
        userRep.setRequiredActions(requiredActions);

        log.debug(
                "Converted PersistableCustomer to UserRepresentation, username={}, required actions: {}",
                userRep.getUsername(),
                requiredActions
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

    public CreatedUserResponse toCreatedUser(UserRepresentation userRep) {
        CreatedUserResponse createdUserResponse = customerMapper.toCreatedUser(userRep);
        createdUserResponse.setId(UUID.fromString(userRep.getId()));
        log.debug(
                "Converted UserRepresentation to CreatedUser, userid={}",
                userRep.getId()
        );
        return createdUserResponse;
    }


}
