package vn.io.oldmoon.shopizer.user.app.facade;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.bussiness.service.keycloak.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
public class CustomerFacade {
    private final CustomerProfileRepository profileRepo;
    private final CustomerPopulator customerPopulator;
    private final KeycloakService keycloakService;
    // private final CustomerService customerService
    // private final TokenService tokenService
    // private final EmailService emailService

    @NewSpan
    public CreatedUser registerCustomer(PersistableCustomer persistableCustomer) {
        UserRepresentation userRep = customerPopulator.toUserRep(persistableCustomer);
        CustomerProfile profile = customerPopulator.toProfile(persistableCustomer);

        String userId = keycloakService.create(userRep);
        userRep.setId(userId);
        keycloakService.resetPassword(userId, persistableCustomer.getPassword());

        // tokenService.generateCode()
        // token(userId, email, code, expiresAt, type=EMAIL_VERIFICATION)
        // async
        //      customerService.create(profile)
        //      tokenService.create(token)
        //      emailService.sendEmailVerifyCode(customer, email, code)

        return customerPopulator.toCreatedUser(userRep);
    }

}
