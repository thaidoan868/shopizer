package vn.io.oldmoon.shopizer.user.app.populator.customer;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.AvatarResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.populator.converter.UrlConverter;
import vn.io.oldmoon.shopizer.user.infra.data.constant.KeycloakRequiredAction;
import vn.io.oldmoon.shopizer.user.infra.model.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPopulator {
  private final CustomerMapper customerMapper;
  private final UrlConverter urlConverter;

  // TO MODELS
  public UserRepresentation toUserRep(PersistableCustomer persistableCustomer) {
    List<String> requiredActions = List.of(KeycloakRequiredAction.VERIFY_EMAIL.toString());
    UserRepresentation userRep = customerMapper.toUserRep(persistableCustomer);
    userRep.setEnabled(true);
    userRep.setEmailVerified(false);
    userRep.setRequiredActions(requiredActions);

    log.debug(
        "Converted PersistableCustomer to UserRepresentation, username={}, required actions: {}",
        userRep.getUsername(),
        requiredActions);
    return userRep;
  }

  public CustomerProfile toProfile(PersistableCustomer persistableCustomer) {
    var result = customerMapper.toProfile(persistableCustomer);
    log.debug(
        "Converted PersistableCustomer to CustomerProfile, username={}",
        persistableCustomer.getUsername());
    return result;
  }

  // To RESPONSES
  public CreatedUserResponse toCreatedUser(UserRepresentation userRep) {
    CreatedUserResponse response = customerMapper.toCreatedUser(userRep);

    response.setId(UUID.fromString(userRep.getId()));

    log.debug("Converted UserRepresentation to CreatedUser, userid={}", userRep.getId());
    return response;
  }

  private AvatarResponse toAvatarResponse(AvatarMeta avatar) {
    if (avatar == null) {
      return null;
    }
    return new AvatarResponse(
        urlConverter.media(avatar.bucket(), avatar.originalObjectName()),
        urlConverter.media(avatar.bucket(), avatar.mediumObjectName()),
        urlConverter.media(avatar.bucket(), avatar.thumbnailObjectName()));
  }

  public CustomerProfileResponse toProfileResponse(CustomerProfile profile) {
    CustomerProfileResponse response = customerMapper.toProfileResponse(profile);

    // set avatar urls
    AvatarMeta avatar = profile.getAvatarMeta();
    response.setAvatarMeta(toAvatarResponse(avatar));

    log.debug(
        "Converted CustomerProfile to CustomerProfileResponse: userId={}", profile.getUserId());
    return response;
  }

  public PublicCustomerProfileResponse toPublicProfileResponse(CustomerProfile profile) {
    PublicCustomerProfileResponse response = customerMapper.toPublicProfileResponse(profile);

    // set avatar urls
    AvatarMeta avatar = profile.getAvatarMeta();
    response.setAvatarMeta(toAvatarResponse(avatar));

    log.debug(
        "Converted CustomerProfile to PublicCustomerProfileResponse: userId={}",
        profile.getUserId());
    return response;
  }
}
