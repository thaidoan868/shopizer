package vn.io.oldmoon.shopizer.common.web.controller;

import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.web.model.UserRepresentation;

@Slf4j
public class AbstractController {
  /**
   * @throws ApiException when the current user is not logged in
   */
  public static Optional<UserRepresentation> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      log.warn("Trying to get the current user but there is no authentication provided");
      return Optional.empty();
    }

    UserRepresentation user = new UserRepresentation();

    user.setUsername(jwt.getClaimAsString("preferred_username"));
    user.setId(jwt.getSubject());
    user.setFirstName(jwt.getClaimAsString("given_name"));
    user.setLastName(jwt.getClaimAsString("family_name"));
    user.setEmail(jwt.getClaimAsString("email"));

    log.info("Fetched User: userId={}", jwt.getSubject());
    return Optional.of(user);
  }

  /**
   * @throws ApiException when the current user is not logged in
   */
  public static Optional<UUID> getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      log.warn("Trying to get the current user id but there is no authentication provided");
      return Optional.empty();
    }

    log.info("Fetched UserId: userId={}", jwt.getSubject());
    UUID userId = UUID.fromString(jwt.getSubject());
    return Optional.of(userId);
  }
}
