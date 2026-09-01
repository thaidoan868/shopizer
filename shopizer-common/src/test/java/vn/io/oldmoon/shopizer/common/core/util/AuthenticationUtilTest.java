package vn.io.oldmoon.shopizer.common.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.io.oldmoon.shopizer.common.web.model.UserRepresentation;

class AuthenticationUtilTest {

  @BeforeEach
  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("getCurrentUser Tests")
  class GetCurrentUserTests {

    @Test
    @DisplayName("Should return empty Optional when Authentication is null")
    void shouldReturnEmptyWhenAuthenticationIsNull() {
      Optional<UserRepresentation> result = AuthenticationUtil.getCurrentUser();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Optional when Principal is not an instance of Jwt")
    void shouldReturnEmptyWhenPrincipalIsNotJwt() {
      Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", "password");
      SecurityContextHolder.getContext().setAuthentication(auth);

      Optional<UserRepresentation> result = AuthenticationUtil.getCurrentUser();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return mapped UserRepresentation when Jwt authentication exists")
    void shouldReturnUserRepresentationWhenJwtExists() {
      String userId = UUID.randomUUID().toString();
      Jwt jwt = mock(Jwt.class);

      when(jwt.getSubject()).thenReturn(userId);
      when(jwt.getClaimAsString("preferred_username")).thenReturn("john_doe");
      when(jwt.getClaimAsString("given_name")).thenReturn("John");
      when(jwt.getClaimAsString("family_name")).thenReturn("Doe");
      when(jwt.getClaimAsString("email")).thenReturn("john.doe@example.com");

      Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null);
      SecurityContextHolder.getContext().setAuthentication(auth);

      Optional<UserRepresentation> result = AuthenticationUtil.getCurrentUser();

      assertThat(result).isPresent();
      UserRepresentation user = result.get();
      assertThat(user.getId()).isEqualTo(userId);
      assertThat(user.getUsername()).isEqualTo("john_doe");
      assertThat(user.getFirstName()).isEqualTo("John");
      assertThat(user.getLastName()).isEqualTo("Doe");
      assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }
  }

  @Nested
  @DisplayName("getCurrentUserId Tests")
  class GetCurrentUserIdTests {

    @Test
    @DisplayName("Should return empty Optional when Authentication is null")
    void shouldReturnEmptyWhenAuthenticationIsNull() {
      Optional<UUID> result = AuthenticationUtil.getCurrentUserId();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Optional when Principal is not a Jwt")
    void shouldReturnEmptyWhenPrincipalIsNotJwt() {
      Authentication auth = new UsernamePasswordAuthenticationToken("user", "password");
      SecurityContextHolder.getContext().setAuthentication(auth);

      Optional<UUID> result = AuthenticationUtil.getCurrentUserId();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return UUID when Jwt subject is a valid UUID string")
    void shouldReturnUUIDWhenValidJwtSubjectExists() {
      UUID expectedUserId = UUID.randomUUID();
      Jwt jwt = mock(Jwt.class);
      when(jwt.getSubject()).thenReturn(expectedUserId.toString());

      Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null);
      SecurityContextHolder.getContext().setAuthentication(auth);

      Optional<UUID> result = AuthenticationUtil.getCurrentUserId();
      assertThat(result).contains(expectedUserId);
    }

    @Test
    @DisplayName(
        "Should throw IllegalArgumentException when Jwt subject is not a valid UUID string")
    void shouldThrowExceptionWhenJwtSubjectIsNotValidUUID() {
      Jwt jwt = mock(Jwt.class);
      when(jwt.getSubject()).thenReturn("not-a-valid-uuid");

      Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null);
      SecurityContextHolder.getContext().setAuthentication(auth);

      assertThatThrownBy(AuthenticationUtil::getCurrentUserId)
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
