package vn.io.oldmoon.shopizer.common.util;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.oldmoon.shopizer.user.business.exception.BusinessException;
import vn.io.oldmoon.shopizer.user.business.exception.ErrorCode;

@Slf4j
public class UserUtil {
  /**
   * @throws BusinessException when the current user is not logged in
   */
  @NonNull
  public static UserRepresentation getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED, "The current user is not logged in");
    }

    UserRepresentation user = new UserRepresentation();

    user.setUsername(jwt.getClaimAsString("preferred_username"));
    user.setId(jwt.getSubject());
    user.setFirstName(jwt.getClaimAsString("given_name"));
    user.setLastName(jwt.getClaimAsString("family_name"));
    user.setEmail(jwt.getClaimAsString("email"));
    user.setEnabled(true);

    log.debug("Fetched User: userId={}", jwt.getSubject());
    return user;
  }

  /**
   * @throws BusinessException when the current user is not logged in
   */
  @NonNull
  public static UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED, "The current user is not logged in");
    }

    log.debug("Fetched UserId: userId={}", jwt.getSubject());
    return UUID.fromString(jwt.getSubject());
  }
}
