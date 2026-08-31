package vn.io.oldmoon.shopizer.user.app.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.handler.BusinessExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.user.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerProfileDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@WebMvcTest(controllers = CustomerController.class)
@ContextConfiguration(
    classes = {
      CustomerController.class,
      SecurityConfig.class,
      BusinessExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class CustomerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;
  @MockitoBean private CustomerProfileService customerProfileService;
  @MockitoBean private CustomerPopulator customerPopulator;

  @Test
  @DisplayName(
      "GET /api/v1/customer/profile with CUSTOMER role should return 200 and CustomerProfileDto")
  void getProfile_WithCustomerRole_ShouldReturn200AndProfileDto() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(userId).username("alice").build();
    CustomerProfile profile = CustomerProfile.builder().user(user).build();

    CustomerProfileDto profileDto =
        CustomerProfileDto.builder()
            .id(UUID.randomUUID())
            .keycloakUserId(userId)
            .username("alice")
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Smith")
            .verified(true)
            .gender(Gender.female)
            .dateOfBirth(LocalDate.of(1995, 5, 20))
            .language(Language.en)
            .phoneNumber("+1234567890")
            .address("123 Main St")
            .avatarMeta(
                new AvatarDto("http://cdn/orig.jpg", "http://cdn/med.jpg", "http://cdn/thumb.jpg"))
            .build();

    given(userService.get(userId)).willReturn(user);
    given(customerProfileService.get(userId)).willReturn(profile);
    given(customerPopulator.toCustomerProfileDto(user, profile)).willReturn(profileDto);

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(userId.toString())
                                    .claim("preferred_username", "alice")
                                    .claim(
                                        "realm_access",
                                        Map.of("roles", List.of(Role.CUSTOMER.toString()))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keycloakUserId").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.email").value("alice@example.com"))
        .andExpect(jsonPath("$.firstName").value("Alice"))
        .andExpect(jsonPath("$.lastName").value("Smith"))
        .andExpect(jsonPath("$.verified").value(true))
        .andExpect(jsonPath("$.gender").value("female"))
        .andExpect(jsonPath("$.dateOfBirth").value("1995-05-20"))
        .andExpect(jsonPath("$.language").value("en"))
        .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
        .andExpect(jsonPath("$.address").value("123 Main St"))
        .andExpect(jsonPath("$.avatarMeta.originalAvatarUrl").value("http://cdn/orig.jpg"))
        .andExpect(jsonPath("$.avatarMeta.mediumAvatarUrl").value("http://cdn/med.jpg"))
        .andExpect(jsonPath("$.avatarMeta.thumbnailAvatarUrl").value("http://cdn/thumb.jpg"));
  }

  @Test
  @DisplayName("GET /api/v1/customer/profile without authentication should return 401 Unauthorized")
  void getProfile_WithoutAuth_ShouldReturn401() throws Exception {
    mockMvc.perform(get("/api/v1/customer/profile")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName(
      "GET /api/v1/customer/profile with non-CUSTOMER role (e.g. ADMIN only) should return 403 Forbidden")
  void getProfile_WithAdminRoleOnly_ShouldReturn403() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(userId.toString())
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("ADMIN"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/v1/customer/profile when profile not found should return 404 Not Found")
  void getProfile_WhenProfileNotFound_ShouldReturn404() throws Exception {
    UUID userId = UUID.randomUUID();
    User user = User.builder().keycloakUserId(userId).username("alice").build();

    given(userService.get(userId)).willReturn(user);
    given(customerProfileService.get(userId))
        .willThrow(new ResourceNotFoundException("Profile", "userId=" + userId));

    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(userId.toString())
                                    .claim("preferred_username", "alice")
                                    .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Resource not found"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("GET /api/v1/customer/profile when user not found should return 404 Not Found")
  void getProfile_WhenUserNotFound_ShouldReturn404() throws Exception {
    UUID userId = UUID.randomUUID();

    given(userService.get(userId))
        .willThrow(new ResourceNotFoundException("User", "userId=" + userId));

    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(userId.toString())
                                    .claim("preferred_username", "alice")
                                    .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Resource not found"))
        .andExpect(jsonPath("$.message").exists());
  }
}
