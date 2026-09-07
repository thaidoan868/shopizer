package vn.io.oldmoon.shopizer.user.business.service.keycloak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

  private static final String REALM = "test-realm";

  @Mock private Keycloak keycloak;
  @Mock private RealmResource realmResource;
  @Mock private UsersResource usersResource;
  @Mock private UserResource userResource;
  @Mock private RolesResource rolesResource;
  @Mock private RoleResource roleResource;
  @Mock private RoleMappingResource roleMappingResource;
  @Mock private RoleScopeResource roleScopeResource;

  @InjectMocks private KeycloakService keycloakService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(keycloakService, "realm", REALM);
  }

  @Nested
  @DisplayName("get(UUID userId)")
  class GetTest {

    @Test
    @DisplayName("should return UserRepresentation when user exists")
    void get_WhenUserExists_ShouldReturnUserRepresentation() {
      // Given
      UUID userId = UUID.randomUUID();
      UserRepresentation expectedUser = new UserRepresentation();
      expectedUser.setId(userId.toString());
      expectedUser.setUsername("john.doe");
      expectedUser.setEmail("john.doe@example.com");

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.get(userId.toString())).thenReturn(userResource);
      when(userResource.toRepresentation()).thenReturn(expectedUser);

      // When
      UserRepresentation actualUser = keycloakService.get(userId);

      // Then
      assertThat(actualUser).isNotNull().isEqualTo(expectedUser);
      verify(keycloak).realm(REALM);
      verify(realmResource).users();
      verify(usersResource).get(userId.toString());
      verify(userResource).toRepresentation();
    }

    @Test
    @DisplayName("should propagate NotFoundException when user does not exist")
    void get_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
      // Given
      UUID userId = UUID.randomUUID();

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.get(userId.toString())).thenReturn(userResource);
      when(userResource.toRepresentation()).thenThrow(new NotFoundException("User not found"));

      // When & Then
      assertThatThrownBy(() -> keycloakService.get(userId))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");
      verify(userResource).toRepresentation();
    }

    @Test
    @DisplayName("should throw NullPointerException when userId is null")
    void get_WhenUserIdIsNull_ShouldThrowNullPointerException() {
      assertThatThrownBy(() -> keycloakService.get(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("getUserByUsername(String username)")
  class GetUserByUsernameTest {

    @Test
    @DisplayName("should return UserRepresentation when user exists")
    void getUserByUsername_WhenUserExists_ShouldReturnUserRepresentation() {
      // Given
      String username = "john.doe";
      UserRepresentation expectedUser = new UserRepresentation();
      expectedUser.setId(UUID.randomUUID().toString());
      expectedUser.setUsername(username);
      expectedUser.setEmail("john.doe@example.com");

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.searchByUsername(username, true)).thenReturn(List.of(expectedUser));

      // When
      UserRepresentation actualUser = keycloakService.getUserByUsername(username);

      // Then
      assertThat(actualUser).isNotNull().isEqualTo(expectedUser);
      verify(usersResource).searchByUsername(username, true);
    }

    @Test
    @DisplayName("should trim username and return UserRepresentation when username has leading/trailing whitespace")
    void getUserByUsername_WhenUsernameHasWhitespace_ShouldTrimAndReturnUser() {
      // Given
      String inputUsername = "  john.doe  ";
      String trimmedUsername = "john.doe";
      UserRepresentation expectedUser = new UserRepresentation();
      expectedUser.setId(UUID.randomUUID().toString());
      expectedUser.setUsername(trimmedUsername);

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.searchByUsername(trimmedUsername, true)).thenReturn(List.of(expectedUser));

      // When
      UserRepresentation actualUser = keycloakService.getUserByUsername(inputUsername);

      // Then
      assertThat(actualUser).isNotNull().isEqualTo(expectedUser);
      verify(usersResource).searchByUsername(trimmedUsername, true);
    }

    @Test
    @DisplayName("should return first user when multiple users found")
    void getUserByUsername_WhenMultipleUsersFound_ShouldReturnFirstUser() {
      // Given
      String username = "john.doe";
      UserRepresentation firstUser = new UserRepresentation();
      firstUser.setId(UUID.randomUUID().toString());
      firstUser.setUsername(username);

      UserRepresentation secondUser = new UserRepresentation();
      secondUser.setId(UUID.randomUUID().toString());
      secondUser.setUsername(username);

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.searchByUsername(username, true)).thenReturn(List.of(firstUser, secondUser));

      // When
      UserRepresentation actualUser = keycloakService.getUserByUsername(username);

      // Then
      assertThat(actualUser).isNotNull().isEqualTo(firstUser);
      verify(usersResource).searchByUsername(username, true);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user does not exist")
    void getUserByUsername_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
      // Given
      String username = "unknown.user";

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.searchByUsername(username, true)).thenReturn(Collections.emptyList());

      // When & Then
      assertThatThrownBy(() -> keycloakService.getUserByUsername(username))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("KeycloakUser")
          .hasMessageContaining("username=" + username);
      verify(usersResource).searchByUsername(username, true);
    }

    @Test
    @DisplayName("should throw InvalidInputException when username is empty")
    void getUserByUsername_WhenUsernameIsEmpty_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> keycloakService.getUserByUsername(""))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Username must not be blank");
    }

    @Test
    @DisplayName("should throw InvalidInputException when username is only whitespace")
    void getUserByUsername_WhenUsernameIsBlank_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> keycloakService.getUserByUsername("   "))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Username must not be blank");
    }

    @Test
    @DisplayName("should throw NullPointerException when username is null")
    void getUserByUsername_WhenUsernameIsNull_ShouldThrowNullPointerException() {
      assertThatThrownBy(() -> keycloakService.getUserByUsername(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("assignRealmRole(String userId, Role role)")
  class AssignRealmRoleTest {

    @Test
    @DisplayName("should assign realm role to user when role exists")
    void assignRealmRole_WhenUserAndRoleExist_ShouldAssignRole() {
      // Given
      String userId = UUID.randomUUID().toString();
      Role role = Role.CUSTOMER;
      RoleRepresentation roleRepresentation = new RoleRepresentation();
      roleRepresentation.setName(role.name());

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.get(userId)).thenReturn(userResource);
      when(realmResource.roles()).thenReturn(rolesResource);
      when(rolesResource.get(role.name())).thenReturn(roleResource);
      when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
      when(userResource.roles()).thenReturn(roleMappingResource);
      when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

      // When
      keycloakService.assignRealmRole(userId, role);

      // Then
      verify(roleScopeResource).add(Collections.singletonList(roleRepresentation));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when role does not exist in realm")
    void assignRealmRole_WhenRoleNotFound_ShouldThrowResourceNotFoundException() {
      // Given
      String userId = UUID.randomUUID().toString();
      Role role = Role.CUSTOMER;

      when(keycloak.realm(REALM)).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);
      when(usersResource.get(userId)).thenReturn(userResource);
      when(realmResource.roles()).thenReturn(rolesResource);
      when(rolesResource.get(role.name())).thenReturn(roleResource);
      when(roleResource.toRepresentation()).thenThrow(new NotFoundException("Role not found"));

      // When & Then
      assertThatThrownBy(() -> keycloakService.assignRealmRole(userId, role))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Role")
          .hasMessageContaining("roleName=" + role.name());

      verify(userResource, never()).roles();
      verify(roleScopeResource, never()).add(anyList());
    }

    @Test
    @DisplayName("should throw NullPointerException when userId is null")
    void assignRealmRole_WhenUserIdIsNull_ShouldThrowNullPointerException() {
      assertThatThrownBy(() -> keycloakService.assignRealmRole(null, Role.CUSTOMER))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when role is null")
    void assignRealmRole_WhenRoleIsNull_ShouldThrowNullPointerException() {
      String userId = UUID.randomUUID().toString();
      assertThatThrownBy(() -> keycloakService.assignRealmRole(userId, null))
          .isInstanceOf(NullPointerException.class);
    }
  }
}
