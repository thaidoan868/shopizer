package vn.io.oldmoon.shopizer.user.app.facade;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.bussiness.service.TokenService;
import vn.io.oldmoon.shopizer.user.bussiness.service.keycloak.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.Token;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerFacade {
    private final CustomerProfileRepository profileRepo;
    private final CustomerPopulator customerPopulator;
    private final KeycloakService keycloakService;
    private final TokenService tokenService;
    // private final CustomerService customerService
    // private final TokenService tokenService
    // private final EmailService emailService

    @NewSpan
    public CreatedUser registerCustomer(PersistableCustomer persistableCustomer) {
        UserRepresentation userRep = customerPopulator.toUserRep(persistableCustomer);
        CustomerProfile profile = customerPopulator.toProfile(persistableCustomer);

        // create a new user
        String userId = keycloakService.create(userRep);
        userRep.setId(userId);
        keycloakService.resetPassword(userId, persistableCustomer.getPassword());


        // generate email verification token
        Token token = Token.builder()
                .email(userRep.getEmail())
                .userId(UUID.fromString(userId))
                .code(tokenService.generateCode())
                .expiresAt(Instant.now().plusSeconds(5 * 60))
                .build();
        tokenService.create(token);

        // async
        profile.setUserId(UUID.fromString(userId));

        //      customerService.create(profile)
        //      tokenService.create(token)
        //      emailService.sendEmailVerifyCode(customer, email, code)

        return customerPopulator.toCreatedUser(userRep);
    }

}
