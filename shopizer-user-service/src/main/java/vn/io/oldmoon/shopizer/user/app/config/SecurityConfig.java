package vn.io.oldmoon.shopizer.user.app.config;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

  @Bean
  public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return jwt -> {
      Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
      String principalName = jwt.getClaimAsString("preferred_username");
      return new JwtAuthenticationToken(jwt, authorities, principalName);
    };
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    // 1. Safely extract the map using the JWT's built-in helper
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

    if (realmAccess == null || !realmAccess.containsKey("roles")) {
      return Collections.emptyList();
    }

    // 2. Extract and Map roles
    @SuppressWarnings("unchecked")
    Collection<String> roles = (Collection<String>) realmAccess.get("roles");

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  // Anyone is allowed to read docs in dev environment
  @Bean
  @Order(1)
  @Profile({"dev", "test"})
  SecurityFilterChain documentEndpoints(HttpSecurity http) throws Exception {
    return http.securityMatcher(
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }

  @Bean
  @Order(2)
  SecurityFilterChain publicEndpoints(HttpSecurity http) throws Exception {
    return http.securityMatcher("/api/v1/public/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }

  @Bean
  @Order(3)
  public SecurityFilterChain privateEndpoints(
      HttpSecurity http, Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

    return http.build();
  }
}
