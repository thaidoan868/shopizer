package vn.io.oldmoon.shopizer.common.web.controller;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.common.web.model.UserRepresentation;

@Slf4j
public class AbstractController {
  /**
   * @throws ApiException when the current user is not logged in
   */
  public static UserRepresentation getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new ApiException(ErrorCode.UNAUTHORIZED, "The current user is not logged in");
    }

    UserRepresentation user = new UserRepresentation();

    user.setUsername(jwt.getClaimAsString("preferred_username"));
    user.setId(jwt.getSubject());
    user.setFirstName(jwt.getClaimAsString("given_name"));
    user.setLastName(jwt.getClaimAsString("family_name"));
    user.setEmail(jwt.getClaimAsString("email"));

    log.debug("Fetched User: userId={}", jwt.getSubject());
    return user;
  }

  /**
   * @throws ApiException when the current user is not logged in
   */
  public static UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new ApiException(ErrorCode.UNAUTHORIZED, "The current user is not logged in");
    }

    log.debug("Fetched UserId: userId={}", jwt.getSubject());
    return UUID.fromString(jwt.getSubject());
  }
}
