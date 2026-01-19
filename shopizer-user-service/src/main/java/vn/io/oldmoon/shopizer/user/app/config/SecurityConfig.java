package vn.io.oldmoon.shopizer.user.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
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
        Collection<String> realmRoles = Optional.ofNullable((Map<String, Object>) jwt.getClaim("realm_access"))
                .map(m -> (Collection<String>) m.get("roles"))
                .orElseGet(List::of);

        return realmRoles.stream()
                .map(r -> (GrantedAuthority) () -> "ROLE_" + r.toUpperCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http, Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v*/users/admin").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/v*/users/details"
                        ).authenticated()
                        .requestMatchers(
                                "/api/v*/users/"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }
}

