package vn.io.oldmoon.shopizer.user.app.facade;

import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.bussiness.service.CustomerService;
import vn.io.oldmoon.shopizer.user.bussiness.service.EmailService;
import vn.io.oldmoon.shopizer.user.bussiness.service.TokenService;
import vn.io.oldmoon.shopizer.user.bussiness.service.keycloak.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.EmailTemplate;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.Token;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerFacade {
    private final TaskExecutor taskExecutor;

    private final CustomerProfileRepository profileRepo;
    private final CustomerPopulator customerPopulator;
    private final KeycloakService keycloakService;
    private final TokenService tokenService;
    private final CustomerService customerService;
    private final EmailService emailService;

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
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(5 * 60))
                .build();

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
