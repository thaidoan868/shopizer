package vn.io.oldmoon.shopizer.product.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityConfigTest {

  private final SecurityConfig securityConfig = new SecurityConfig();

  @Test
  @DisplayName("should extract authorities from realm roles and set principal name")
  void shouldExtractAuthoritiesFromRealmRoles() {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("preferred_username", "adminuser")
            .claim("realm_access", Map.of("roles", List.of("super_admin", "admin")))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    AbstractAuthenticationToken auth =
        securityConfig.jwtAuthenticationConverter().convert(jwt);

    assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
    Set<String> authorities =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertThat(authorities).containsExactlyInAnyOrder("ROLE_SUPER_ADMIN", "ROLE_ADMIN");
    assertThat(auth.getName()).isEqualTo("adminuser");
  }

  @Test
  @DisplayName("should return empty authorities when realm_access claim is missing")
  void shouldReturnEmptyAuthoritiesWhenRealmAccessMissing() {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("preferred_username", "guest")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    AbstractAuthenticationToken auth =
        securityConfig.jwtAuthenticationConverter().convert(jwt);

    assertThat(auth).isNotNull();
    assertThat(auth.getAuthorities()).isEmpty();
  }

  @Test
  @DisplayName("should return empty authorities when roles key is missing in realm_access")
  void shouldReturnEmptyAuthoritiesWhenRolesKeyMissing() {
    Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("preferred_username", "guest")
            .claim("realm_access", Map.of())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    AbstractAuthenticationToken auth =
        securityConfig.jwtAuthenticationConverter().convert(jwt);

    assertThat(auth).isNotNull();
    assertThat(auth.getAuthorities()).isEmpty();
  }
}
