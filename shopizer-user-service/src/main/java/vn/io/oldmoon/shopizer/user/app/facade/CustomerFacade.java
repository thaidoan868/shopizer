package vn.io.oldmoon.shopizer.user.app.facade;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.bussiness.service.CustomerService;
import vn.io.oldmoon.shopizer.user.bussiness.service.EmailService;
import vn.io.oldmoon.shopizer.user.bussiness.service.TokenService;
import vn.io.oldmoon.shopizer.user.bussiness.service.keycloak.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.EmailTemplate;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.Token;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerFacade {
    private final TaskExecutor taskExecutor;

    private final CustomerPopulator customerPopulator;
    private final KeycloakService keycloakService;
    private final TokenService tokenService;
    private final CustomerService customerService;
    private final EmailService emailService;

    public CreatedUserResponse registerCustomer(PersistableCustomer persistableCustomer) {
        UserRepresentation userRep = customerPopulator.toUserRep(persistableCustomer);
        CustomerProfile profile = customerPopulator.toProfile(persistableCustomer);

        // create a new user
        String userId = keycloakService.create(userRep);
        userRep.setId(userId);

        // set new password
        keycloakService.resetPassword(userId, persistableCustomer.getPassword());

        // assign role CUSTOMER
        keycloakService.assignRealmRole(userId, Role.customer);

        // generate email verification token
        Token token = new Token(
                userRep.getUsername(),
                userRep.getEmail(),
                tokenService.generateCode(),
                TokenType.EMAIL_VERIFICATION
        );
        token.setUserId(UUID.fromString(userId));

        // async
        // create a profile
        profile.setUserId(UUID.fromString(userId));
        taskExecutor.execute(() -> customerService.createProfile(profile));

        // create a token
        taskExecutor.execute(() -> tokenService.create(token));

        // send a verification email
        taskExecutor.execute(() ->
                emailService.sendMail(
                        userRep.getEmail(),
                        EmailTemplate.EMAIL_VERIFICATION_SUBJECT,
                        EmailTemplate.verifyEmail(profile.getFullName(), token.getCode())
                )
        );

        return customerPopulator.toCreatedUser(userRep);
    }
}
