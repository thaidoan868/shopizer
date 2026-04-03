package vn.io.oldmoon.shopizer.user.app.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.UpdateCustomerProfileRequest;
import vn.io.oldmoon.shopizer.user.app.transfer.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.business.service.CustomerService;
import vn.io.oldmoon.shopizer.user.business.service.EmailService;
import vn.io.oldmoon.shopizer.user.business.service.KeycloakService;
import vn.io.oldmoon.shopizer.user.business.service.TokenService;
import vn.io.oldmoon.shopizer.user.infra.data.EmailTemplate;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.Token;

@Service
@RequiredArgsConstructor
public class CustomerFacade {
  private final CustomerPopulator customerPopulator;
  private final KeycloakService keycloakService;
  private final TokenService tokenService;
  private final CustomerService customerService;
  private final EmailService emailService;

  @Deprecated
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
    Token token =
        new Token(
            userRep.getUsername(),
            userRep.getEmail(),
            tokenService.generateCode(),
            TokenType.EMAIL_VERIFICATION);
    token.setUserId(UUID.fromString(userId));

    // async
    // create a profile
    profile.setUserId(UUID.fromString(userId));
    customerService.createProfile(profile);

    // create a token
    tokenService.create(token);

    // send a verification email
    emailService.sendMail(
        userRep.getEmail(),
        EmailTemplate.EMAIL_VERIFICATION_SUBJECT,
        EmailTemplate.verifyEmail(profile.getFullName(), token.getCode()));

    return customerPopulator.toCreatedUser(userRep);
  }

  public CustomerProfileResponse getProfile(UUID userId) {
    // get profile
    CustomerProfile profile = customerService.get(userId);

    return customerPopulator.toProfileResponse(profile);
  }

  public PublicCustomerProfileResponse getPublicProfile(UUID userId) {
    // get profile
    CustomerProfile profile = customerService.get(userId);

    return customerPopulator.toPublicProfileResponse(profile);
  }

  public CustomerProfileResponse updateProfile(UUID userId, UpdateCustomerProfileRequest request) {
    // get the current user's profile
    CustomerProfile profile = customerService.get(userId);

    // update the profile and save the updated profile
    customerPopulator.patchUpdate(request, profile);
    CustomerProfile newProfile = customerService.updateProfile(profile);

    // convert to response
    return customerPopulator.toProfileResponse(newProfile);
  }
}
