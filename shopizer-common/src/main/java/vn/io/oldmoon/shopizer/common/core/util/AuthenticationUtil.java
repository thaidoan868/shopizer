package vn.io.oldmoon.shopizer.common.core.util;

import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.oldmoon.shopizer.common.web.model.UserRepresentation;

@Slf4j
public class AuthenticationUtil {
  /**
   * Fetches the current user id from the security context. If the user is not logged in, it returns
   * an empty Optional.
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

  /**
   * Fetches the current user representation from the security context. If the user is not logged
   * in, it returns an empty Optional.
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

    log.debug("Fetched User from security context: userId={}", jwt.getSubject());
    return Optional.of(user);
  }
}
