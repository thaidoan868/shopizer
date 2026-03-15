package vn.io.oldmoon.shopizer.user.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import vn.io.oldmoon.shopizer.user.app.system.config.SecurityConfig;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void shouldExtractAuthoritiesFromRealmRoles() {
        // given
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .claim("realm_access", Map.of(
                        "roles", List.of("admin", "user")
                ))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // when
        AbstractAuthenticationToken authentication =
                securityConfig.jwtAuthenticationConverter().convert(jwt);

        // then
        assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");

        assertThat(authentication.getName()).isEqualTo("alice");
    }
}
