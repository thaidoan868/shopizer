package vn.io.oldmoon.shopizer.user.app.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerProfileIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CustomerProfileRepository customerProfileRepository;

  @BeforeEach
  void setUp() {
    customerProfileRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("Happy Path: Fetch profile for authenticated Customer with existing User and CustomerProfile")
  void getProfile_WhenUserAndProfileExist_ShouldReturnCustomerProfileDto() throws Exception {
    // Given: Persist User and CustomerProfile
    UUID keycloakUserId = UUID.randomUUID();
    AvatarMeta avatarMeta =
        new AvatarMeta("public-assets", "avatar.png", "avatar-med.png", "avatar-thumb.png");

    User user =
        User.builder()
            .realm("shopizer")
            .keycloakUserId(keycloakUserId)
            .username("johndoe_it")
            .email("johndoe_it@example.com")
            .firstName("John")
            .lastName("Doe")
            .verified(true)
            .avatarMeta(avatarMeta)
            .build();
    user = userRepository.save(user);

    CustomerProfile profile =
        CustomerProfile.builder()
            .user(user)
            .language(Language.en)
            .phoneNumber("+84901234567")
            .address("123 Nguyen Hue, Quan 1, TP HCM")
            .dateOfBirth(LocalDate.of(1992, 8, 15))
            .gender(Gender.male)
            .build();
    customerProfileRepository.save(profile);

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(keycloakUserId.toString())
                                    .claim("preferred_username", "johndoe_it")
                                    .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keycloakUserId").value(keycloakUserId.toString()))
        .andExpect(jsonPath("$.username").value("johndoe_it"))
        .andExpect(jsonPath("$.email").value("johndoe_it@example.com"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.verified").value(true))
        .andExpect(jsonPath("$.language").value("en"))
        .andExpect(jsonPath("$.phoneNumber").value("+84901234567"))
        .andExpect(jsonPath("$.address").value("123 Nguyen Hue, Quan 1, TP HCM"))
        .andExpect(jsonPath("$.dateOfBirth").value("1992-08-15"))
        .andExpect(jsonPath("$.gender").value("male"))
        .andExpect(
            jsonPath("$.avatarMeta.originalAvatarUrl")
                .value("http://localhost:9000/public-assets/avatar.png"))
        .andExpect(
            jsonPath("$.avatarMeta.mediumAvatarUrl")
                .value("http://localhost:9000/public-assets/avatar-med.png"))
        .andExpect(
            jsonPath("$.avatarMeta.thumbnailAvatarUrl")
                .value("http://localhost:9000/public-assets/avatar-thumb.png"));
  }

  @Test
  @DisplayName("Bad Path: When User exists but CustomerProfile is missing, return 404 Not Found")
  void getProfile_WhenUserExistsWithoutCustomerProfile_ShouldReturn404() throws Exception {
    // Given: Persist User only (no CustomerProfile)
    UUID keycloakUserId = UUID.randomUUID();
    User user =
        User.builder()
            .realm("shopizer")
            .keycloakUserId(keycloakUserId)
            .username("orphaned_user")
            .email("orphaned@example.com")
            .firstName("Orphan")
            .lastName("User")
            .build();
    userRepository.save(user);

    // When & Then
    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(keycloakUserId.toString())
                                    .claim("preferred_username", "orphaned_user")
                                    .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Resource not found"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("Bad Path: When User does not exist, return 404 Not Found")
  void getProfile_WhenUserDoesNotExist_ShouldReturn404() throws Exception {
    UUID nonExistentUserId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(nonExistentUserId.toString())
                                    .claim("preferred_username", "non_existent")
                                    .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Resource not found"));
  }

  @Test
  @DisplayName("Security: Without CUSTOMER role (e.g. ADMIN only), return 403 Forbidden")
  void getProfile_WithAdminRoleOnly_ShouldReturn403() throws Exception {
    UUID keycloakUserId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/api/v1/customer/profile")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .subject(keycloakUserId.toString())
                                    .claim("preferred_username", "admin_user")
                                    .claim("realm_access", Map.of("roles", List.of("ADMIN"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Security: Unauthenticated request should return 401 Unauthorized")
  void getProfile_WithoutAuth_ShouldReturn401() throws Exception {
    mockMvc
        .perform(get("/api/v1/customer/profile").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}
