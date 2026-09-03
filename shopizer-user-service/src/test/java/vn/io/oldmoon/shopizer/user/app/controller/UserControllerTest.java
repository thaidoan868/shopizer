package vn.io.oldmoon.shopizer.user.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.handler.ApplicationExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.user.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(
    classes = {
      UserController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;
  @MockitoBean private UserPopulator userPopulator;

  private byte[] createValidImageBytes() {
    try {
      BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(img, "png", baos);
      return baos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/users/me/avatar")
  class UpdateAvatarTests {

    @Test
    @DisplayName("with authenticated user and valid image should return 200 and UserDto")
    void updateAvatar_WhenAuthenticatedAndValidImage_ShouldReturn200AndUserDto() throws Exception {
      // Given
      UUID userId = UUID.randomUUID();
      byte[] imageBytes = createValidImageBytes();
      MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", imageBytes);

      AvatarMeta avatarMeta =
          new AvatarMeta(
              "public-assets",
              "avatar-" + userId + "-orig.png",
              "avatar-medium-" + userId + "-med.jpg",
              "avatar-thumbnail-" + userId + "-thumb.jpg");

      User updatedUser =
          User.builder().keycloakUserId(userId).username("testuser").avatarMeta(avatarMeta).build();

      AvatarDto avatarDto =
          new AvatarDto(
              "http://localhost:9000/public-assets/avatar-" + userId + "-orig.png",
              "http://localhost:9000/public-assets/avatar-medium-" + userId + "-med.jpg",
              "http://localhost:9000/public-assets/avatar-thumbnail-" + userId + "-thumb.jpg");

      UserDto userDto =
          UserDto.builder()
              .keycloakUserId(userId)
              .username("testuser")
              .avatarMeta(avatarDto)
              .build();

      given(userService.updateAvatar(eq(userId), any(MultipartFile.class))).willReturn(updatedUser);
      given(userPopulator.toUserDto(updatedUser)).willReturn(userDto);

      MockMultipartHttpServletRequestBuilder builder =
          MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/users/me/avatar");

      // When & Then
      mockMvc
          .perform(
              builder
                  .file(file)
                  .with(
                      jwt()
                          .jwt(
                              jwtBuilder ->
                                  jwtBuilder
                                      .subject(userId.toString())
                                      .claim("preferred_username", "testuser")
                                      .claim(
                                          "realm_access",
                                          Map.of("roles", List.of(Role.CUSTOMER.toString()))))
                          .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.keycloakUserId").value(userId.toString()))
          .andExpect(jsonPath("$.username").value("testuser"))
          .andExpect(
              jsonPath("$.avatarMeta.originalAvatarUrl")
                  .value("http://localhost:9000/public-assets/avatar-" + userId + "-orig.png"))
          .andExpect(
              jsonPath("$.avatarMeta.mediumAvatarUrl")
                  .value(
                      "http://localhost:9000/public-assets/avatar-medium-" + userId + "-med.jpg"))
          .andExpect(
              jsonPath("$.avatarMeta.thumbnailAvatarUrl")
                  .value(
                      "http://localhost:9000/public-assets/avatar-thumbnail-"
                          + userId
                          + "-thumb.jpg"));
    }

    @Test
    @DisplayName("without authentication should return 401 Unauthorized")
    void updateAvatar_WithoutAuth_ShouldReturn401() throws Exception {
      byte[] imageBytes = createValidImageBytes();
      MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", imageBytes);

      MockMultipartHttpServletRequestBuilder builder =
          MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/users/me/avatar");

      mockMvc
          .perform(builder.file(file).contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("with empty file should return 400 Bad Request")
    void updateAvatar_WithEmptyFile_ShouldReturn400() throws Exception {
      UUID userId = UUID.randomUUID();
      MockMultipartFile emptyFile =
          new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

      MockMultipartHttpServletRequestBuilder builder =
          MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/users/me/avatar");

      mockMvc
          .perform(
              builder
                  .file(emptyFile)
                  .with(
                      jwt()
                          .jwt(jwtBuilder -> jwtBuilder.subject(userId.toString()))
                          .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Bad request"));
    }

    @Test
    @DisplayName("when user not found should return 404 Not Found")
    void updateAvatar_WhenUserNotFound_ShouldReturn404() throws Exception {
      UUID userId = UUID.randomUUID();
      byte[] imageBytes = createValidImageBytes();
      MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", imageBytes);

      given(userService.updateAvatar(eq(userId), any(MultipartFile.class)))
          .willThrow(new ResourceNotFoundException("User", "userId=" + userId));

      MockMultipartHttpServletRequestBuilder builder =
          MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/users/me/avatar");

      mockMvc
          .perform(
              builder
                  .file(file)
                  .with(
                      jwt()
                          .jwt(jwtBuilder -> jwtBuilder.subject(userId.toString()))
                          .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error").value("Resource not found"));
    }

    @Test
    @DisplayName("with avatar field name should return 200 and UserDto")
    void updateAvatar_WithAvatarFieldName_ShouldReturn200() throws Exception {
      UUID userId = UUID.randomUUID();
      byte[] imageBytes = createValidImageBytes();
      MockMultipartFile file =
          new MockMultipartFile("avatar", "avatar.png", "image/png", imageBytes);

      AvatarMeta avatarMeta = new AvatarMeta("public-assets", "orig.png", "med.jpg", "thumb.jpg");
      User updatedUser =
          User.builder().keycloakUserId(userId).username("testuser").avatarMeta(avatarMeta).build();

      AvatarDto avatarDto =
          new AvatarDto("http://cdn/orig.png", "http://cdn/med.jpg", "http://cdn/thumb.jpg");
      UserDto userDto =
          UserDto.builder()
              .keycloakUserId(userId)
              .username("testuser")
              .avatarMeta(avatarDto)
              .build();

      given(userService.updateAvatar(eq(userId), any(MultipartFile.class))).willReturn(updatedUser);
      given(userPopulator.toUserDto(updatedUser)).willReturn(userDto);

      MockMultipartHttpServletRequestBuilder builder =
          MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/users/me/avatar");

      mockMvc
          .perform(
              builder
                  .file(file)
                  .with(
                      jwt()
                          .jwt(jwtBuilder -> jwtBuilder.subject(userId.toString()))
                          .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.keycloakUserId").value(userId.toString()))
          .andExpect(jsonPath("$.avatarMeta.originalAvatarUrl").value("http://cdn/orig.png"));
    }
  }
}
