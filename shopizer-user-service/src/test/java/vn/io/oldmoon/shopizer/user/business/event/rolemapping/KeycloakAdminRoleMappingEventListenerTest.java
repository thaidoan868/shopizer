package vn.io.oldmoon.shopizer.user.business.event.rolemapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminAuthDetails;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEventParser;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminRoleMappingEventListenerTest {

  @Mock private UserService userService;

  @Mock private EmployeeProfileService employeeProfileService;

  @Mock private KeycloakAdminEventParser parser;

  @InjectMocks private KeycloakAdminRoleMappingEventListener listener;

  private UUID userId;
  private UUID adminId;
  private KeycloakAdminEvent event;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    adminId = UUID.randomUUID();
    KeycloakAdminAuthDetails authDetails =
        KeycloakAdminAuthDetails.builder().userId(adminId.toString()).realmName("master").build();
    event =
        KeycloakAdminEvent.builder()
            .resourcePath("users/" + userId + "/role-mappings/realm")
            .resourceType("REALM_ROLE_MAPPING")
            .operationType("CREATE")
            .authDetails(authDetails)
            .build();
  }

  @Nested
  @DisplayName("handle method tests")
  class HandleTests {

    @ParameterizedTest
    @ValueSource(strings = {"STORE_MANAGER", "SUPER_ADMIN", "SUPPORT_AGENT", "WAREHOUSE_STAFF"})
    @DisplayName("Should create employee profile when any trigger role is assigned")
    void handle_WithTriggerRole_ShouldCreateEmployeeProfile(String roleName) {
      // Given
      KeycloakRoleRepresentation role = KeycloakRoleRepresentation.builder().name(roleName).build();
      User user = User.builder().keycloakUserId(userId).username("employee").build();

      when(parser.parseListRepresentations(event, KeycloakRoleRepresentation.class)).thenReturn(List.of(role));
      when(parser.extractUserId(event)).thenReturn(userId);
      when(employeeProfileService.get(userId)).thenReturn(Optional.empty());
      when(userService.get(userId)).thenReturn(user);
      when(employeeProfileService.create((EmployeeProfile) any()))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      listener.handle(event);

      // Then
      verify(parser).parseListRepresentations(event, KeycloakRoleRepresentation.class);
      verify(parser).extractUserId(event);
      verify(employeeProfileService).get(userId);
      verify(userService).get(userId);
      verify(employeeProfileService)
          .create(
              (EmployeeProfile)
                  argThat(
                      profile ->
                          profile instanceof EmployeeProfile ep
                              && ep.getUser().equals(user)
                              && adminId.equals(ep.getCreatedBy())));
    }

    @Test
    @DisplayName("Should create employee profile when multiple roles contain a trigger role")
    void handle_WithMultipleRolesContainingTriggerRole_ShouldCreateEmployeeProfile() {
      // Given
      KeycloakRoleRepresentation customerRole =
          KeycloakRoleRepresentation.builder().name("CUSTOMER").build();
      KeycloakRoleRepresentation storeManagerRole =
          KeycloakRoleRepresentation.builder().name("STORE_MANAGER").build();
      User user = User.builder().keycloakUserId(userId).username("manager").build();

      when(parser.parseListRepresentations(event, KeycloakRoleRepresentation.class))
          .thenReturn(List.of(customerRole, storeManagerRole));
      when(parser.extractUserId(event)).thenReturn(userId);
      when(employeeProfileService.get(userId)).thenReturn(Optional.empty());
      when(userService.get(userId)).thenReturn(user);
      when(employeeProfileService.create((EmployeeProfile) any()))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      listener.handle(event);

      // Then
      verify(employeeProfileService).create((EmployeeProfile) any());
    }

    @Test
    @DisplayName("Should skip creation when roles do not contain any trigger role")
    void handle_WithNonTriggerRole_ShouldSkipCreation() {
      // Given
      KeycloakRoleRepresentation role =
          KeycloakRoleRepresentation.builder().name("CUSTOMER").build();

      when(parser.parseListRepresentations(event, KeycloakRoleRepresentation.class)).thenReturn(List.of(role));

      // When
      listener.handle(event);

      // Then
      verify(parser).parseListRepresentations(event, KeycloakRoleRepresentation.class);
      verify(parser, never()).extractUserId(any());
      verify(employeeProfileService, never()).get(any());
      verify(userService, never()).get(any());
      verify(employeeProfileService, never()).create((EmployeeProfile) any());
    }

    @Test
    @DisplayName("Should skip creation when roles list is empty")
    void handle_WithEmptyRolesList_ShouldSkipCreation() {
      // Given
      when(parser.parseListRepresentations(event, KeycloakRoleRepresentation.class)).thenReturn(Collections.emptyList());

      // When
      listener.handle(event);

      // Then
      verify(parser).parseListRepresentations(event, KeycloakRoleRepresentation.class);
      verify(parser, never()).extractUserId(any());
      verify(employeeProfileService, never()).get(any());
      verify(userService, never()).get(any());
      verify(employeeProfileService, never()).create((EmployeeProfile) any());
    }

    @Test
    @DisplayName("Should skip creation gracefully when employee profile already exists")
    void handle_WhenProfileAlreadyExists_ShouldSkipCreation() {
      // Given
      KeycloakRoleRepresentation role =
          KeycloakRoleRepresentation.builder().name("SUPER_ADMIN").build();
      EmployeeProfile existingProfile =
          EmployeeProfile.builder().user(User.builder().keycloakUserId(userId).build()).build();

      when(parser.parseListRepresentations(event, KeycloakRoleRepresentation.class)).thenReturn(List.of(role));
      when(parser.extractUserId(event)).thenReturn(userId);
      when(employeeProfileService.get(userId)).thenReturn(Optional.of(existingProfile));

      // When
      listener.handle(event);

      // Then
      verify(parser).parseListRepresentations(event, KeycloakRoleRepresentation.class);
      verify(parser).extractUserId(event);
      verify(employeeProfileService).get(userId);
      verify(userService, never()).get(any());
      verify(employeeProfileService, never()).create((EmployeeProfile) any());
    }
  }

  @Nested
  @DisplayName("hasTriggerRole tests")
  class HasTriggerRoleTests {

    @Test
    @DisplayName("Should return false when roles list is null or empty")
    void hasTriggerRole_NullOrEmpty_ShouldReturnFalse() {
      assertThat(listener.hasTriggerRole(null)).isFalse();
      assertThat(listener.hasTriggerRole(Collections.emptyList())).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"STORE_MANAGER", "SUPER_ADMIN", "SUPPORT_AGENT", "WAREHOUSE_STAFF"})
    @DisplayName("Should return true for all valid trigger roles")
    void hasTriggerRole_ValidRoles_ShouldReturnTrue(String roleName) {
      List<KeycloakRoleRepresentation> roles =
          List.of(KeycloakRoleRepresentation.builder().name(roleName).build());
      assertThat(listener.hasTriggerRole(roles)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "ANONYMOUS", "OTHER", "admin"})
    @DisplayName("Should return false for non-trigger roles")
    void hasTriggerRole_NonTriggerRoles_ShouldReturnFalse(String roleName) {
      List<KeycloakRoleRepresentation> roles =
          List.of(KeycloakRoleRepresentation.builder().name(roleName).build());
      assertThat(listener.hasTriggerRole(roles)).isFalse();
    }
  }

  @Nested
  @DisplayName("toEmployeeProfileEntity mapping tests")
  class ToEmployeeProfileEntityTests {

    @Test
    @DisplayName("Should map event authDetails createdBy and user entity correctly")
    void toEmployeeProfileEntity_WithAuthDetails_ShouldSetCreatedBy() {
      User user = User.builder().keycloakUserId(userId).build();
      EmployeeProfile profile = listener.toEmployeeProfileEntity(event, user);

      assertThat(profile).isNotNull();
      assertThat(profile.getUser()).isEqualTo(user);
      assertThat(profile.getCreatedBy()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("Should handle null authDetails without throwing exception")
    void toEmployeeProfileEntity_WithoutAuthDetails_ShouldSetNullCreatedBy() {
      KeycloakAdminEvent eventWithoutAuth =
          KeycloakAdminEvent.builder()
              .resourcePath("users/" + userId + "/role-mappings/realm")
              .authDetails(null)
              .build();

      User user = User.builder().keycloakUserId(userId).build();
      EmployeeProfile profile = listener.toEmployeeProfileEntity(eventWithoutAuth, user);

      assertThat(profile).isNotNull();
      assertThat(profile.getUser()).isEqualTo(user);
      assertThat(profile.getCreatedBy()).isNull();
    }
  }
}
