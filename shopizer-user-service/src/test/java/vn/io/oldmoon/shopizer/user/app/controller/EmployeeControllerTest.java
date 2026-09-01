package vn.io.oldmoon.shopizer.user.app.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.handler.ApplicationExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.user.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.user.app.dto.employee.EmployeePopulator;
import vn.io.oldmoon.shopizer.user.app.dto.employee.EmployeeProfileDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.model.profile.Shift;

@WebMvcTest(controllers = EmployeeController.class)
@ContextConfiguration(
    classes = {
      EmployeeController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class EmployeeControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;
  @MockitoBean private EmployeeProfileService employeeProfileService;
  @MockitoBean private EmployeePopulator employeePopulator;

  @Nested
  @DisplayName("GET /api/v1/employees/me/profile")
  class GetProfileTests {

    @ParameterizedTest
    @EnumSource(
        value = Role.class,
        names = {"STORE_MANAGER", "SUPER_ADMIN", "SUPPORT_AGENT", "WAREHOUSE_STAFF"})
    @DisplayName("with allowed employee role should return 200 and EmployeeProfileDto")
    void getProfile_WithAllowedEmployeeRole_ShouldReturn200AndProfileDto(Role role) throws Exception {
      // Given
      UUID userId = UUID.randomUUID();
      User user = User.builder().keycloakUserId(userId).username("emp_user").build();
      EmployeeProfile profile =
          EmployeeProfile.builder().user(user).shift(Shift.MORNING).workPhone("+1234567890").build();

      EmployeeProfileDto profileDto =
          EmployeeProfileDto.builder()
              .id(UUID.randomUUID())
              .keycloakUserId(userId)
              .username("emp_user")
              .email("emp@example.com")
              .firstName("Jane")
              .lastName("Doe")
              .verified(true)
              .shift(Shift.MORNING)
              .workPhone("+1234567890")
              .avatarMeta(
                  new AvatarDto(
                      "http://cdn/orig.jpg", "http://cdn/med.jpg", "http://cdn/thumb.jpg"))
              .build();

      given(userService.get(userId)).willReturn(user);
      given(employeeProfileService.get(userId)).willReturn(profile);
      given(employeePopulator.toEmployeeProfileDto(user, profile)).willReturn(profileDto);

      // When & Then
      mockMvc
          .perform(
              get("/api/v1/employees/me/profile")
                  .with(
                      jwt()
                          .jwt(
                              jwtBuilder ->
                                  jwtBuilder
                                      .subject(userId.toString())
                                      .claim("preferred_username", "emp_user")
                                      .claim(
                                          "realm_access",
                                          Map.of("roles", List.of(role.toString()))))
                          .authorities(new SimpleGrantedAuthority("ROLE_" + role.name())))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.keycloakUserId").value(userId.toString()))
          .andExpect(jsonPath("$.username").value("emp_user"))
          .andExpect(jsonPath("$.email").value("emp@example.com"))
          .andExpect(jsonPath("$.firstName").value("Jane"))
          .andExpect(jsonPath("$.lastName").value("Doe"))
          .andExpect(jsonPath("$.verified").value(true))
          .andExpect(jsonPath("$.shift").value("MORNING"))
          .andExpect(jsonPath("$.workPhone").value("+1234567890"))
          .andExpect(jsonPath("$.avatarMeta.originalAvatarUrl").value("http://cdn/orig.jpg"))
          .andExpect(jsonPath("$.avatarMeta.mediumAvatarUrl").value("http://cdn/med.jpg"))
          .andExpect(jsonPath("$.avatarMeta.thumbnailAvatarUrl").value("http://cdn/thumb.jpg"));
    }

    @Test
    @DisplayName("without authentication should return 401 Unauthorized")
    void getProfile_WithoutAuth_ShouldReturn401() throws Exception {
      mockMvc.perform(get("/api/v1/employees/me/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("with non-employee role (e.g. CUSTOMER only) should return 403 Forbidden")
    void getProfile_WithCustomerRoleOnly_ShouldReturn403() throws Exception {
      UUID userId = UUID.randomUUID();

      mockMvc
          .perform(
              get("/api/v1/employees/me/profile")
                  .with(
                      jwt()
                          .jwt(
                              jwtBuilder ->
                                  jwtBuilder
                                      .subject(userId.toString())
                                      .claim("preferred_username", "customer_user")
                                      .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                          .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("when employee profile not found should return 404 Not Found")
    void getProfile_WhenProfileNotFound_ShouldReturn404() throws Exception {
      UUID userId = UUID.randomUUID();
      User user = User.builder().keycloakUserId(userId).username("emp_user").build();

      given(userService.get(userId)).willReturn(user);
      given(employeeProfileService.get(userId))
          .willThrow(new ResourceNotFoundException("EmployeeProfile", "userId=" + userId));

      mockMvc
          .perform(
              get("/api/v1/employees/me/profile")
                  .with(
                      jwt()
                          .jwt(
                              jwtBuilder ->
                                  jwtBuilder
                                      .subject(userId.toString())
                                      .claim("preferred_username", "emp_user")
                                      .claim(
                                          "realm_access",
                                          Map.of("roles", List.of(Role.STORE_MANAGER.toString()))))
                          .authorities(new SimpleGrantedAuthority("ROLE_STORE_MANAGER")))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error").value("Resource not found"))
          .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("when user not found should return 404 Not Found")
    void getProfile_WhenUserNotFound_ShouldReturn404() throws Exception {
      UUID userId = UUID.randomUUID();

      given(userService.get(userId))
          .willThrow(new ResourceNotFoundException("User", "userId=" + userId));

      mockMvc
          .perform(
              get("/api/v1/employees/me/profile")
                  .with(
                      jwt()
                          .jwt(
                              jwtBuilder ->
                                  jwtBuilder
                                      .subject(userId.toString())
                                      .claim("preferred_username", "emp_user")
                                      .claim(
                                          "realm_access",
                                          Map.of("roles", List.of(Role.STORE_MANAGER.toString()))))
                          .authorities(new SimpleGrantedAuthority("ROLE_STORE_MANAGER")))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error").value("Resource not found"))
          .andExpect(jsonPath("$.message").exists());
    }
  }
}
