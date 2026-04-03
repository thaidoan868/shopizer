package vn.io.oldmoon.shopizer.user.business.service.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.io.oldmoon.shopizer.user.business.service.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@Testcontainers
@SpringBootTest
class KeycloakServiceTest {

  @Container
  static KeycloakContainer keycloakContainer =
      new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
          .withRealmImportFile("realm-export.json");

  @Autowired private KeycloakService keycloakService;

  @Autowired private Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String realm;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("keycloak.server-url", keycloakContainer::getAuthServerUrl);
  }

  private String createUser(String email) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(email);
    user.setEmail(email);
    user.setEnabled(true);
    user.setFirstName("firstname");
    user.setLastName("lastname");

    Response response = keycloak.realm(realm).users().create(user);

    return CreatedResponseUtil.getCreatedId(response);
  }

  @Test
  void shouldGetUser() {
    // Create
    String email = "user-" + UUID.randomUUID() + "@mail.com";
    String username = email;
    String userId = createUser(email);

    // Fetch via service
    UserRepresentation userByID = keycloakService.get(UUID.fromString(userId));
    UserRepresentation userByUsername = keycloakService.getUserByUsername(username);

    // Assert
    assertThat(userByID.getEmail()).isEqualTo(email);
    assertThat(userByUsername.getEmail()).isEqualTo(email);
  }

  @Test
  void shouldAssignRoleCustomer() {
    // given
    String userId = createUser(UUID.randomUUID() + "@mail.com");
    Role role = Role.customer;

    // when
    keycloakService.assignRealmRole(userId, role);

    // then
    List<RoleRepresentation> roles =
        keycloak.realm(realm).users().get(userId).roles().realmLevel().listAll();

    boolean hasCustomerRole =
        roles.stream().anyMatch(myRole -> myRole.getName().equals(role.name()));

    assertThat(hasCustomerRole).isTrue();
  }

  @Test
  void shouldUpdateUser() {
    // given
    String userId = createUser(UUID.randomUUID() + "@mail.com");
    UserRepresentation oldUserRep = keycloakService.get(UUID.fromString(userId));
    String newFirstName = "newFirstName";
    String newLastName = "newLastName";
    String newEmail = "newemail@gmail.com";

    oldUserRep.setFirstName(newFirstName);
    oldUserRep.setLastName(newLastName);
    oldUserRep.setEmail(newEmail);

    // when
    keycloakService.update(oldUserRep);

    // then
    UserRepresentation newUserRep = keycloakService.get(UUID.fromString(userId));
    assertThat(newUserRep.getFirstName()).isEqualTo(newFirstName);
    assertThat(newUserRep.getLastName()).isEqualTo(newLastName);
    assertThat(newUserRep.getEmail()).isEqualTo(newEmail);
  }

  @TestConfiguration
  static class KeycloakTestConfig {
    @Bean
    @Primary
    Keycloak keycloak() {
      return keycloakContainer.getKeycloakAdminClient();
    }
  }
}
