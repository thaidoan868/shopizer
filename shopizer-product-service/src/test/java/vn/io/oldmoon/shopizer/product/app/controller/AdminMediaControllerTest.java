package vn.io.oldmoon.shopizer.product.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.handler.ApplicationExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.product.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.product.app.dto.media.CreateImageAssetResponse;
import vn.io.oldmoon.shopizer.product.app.dto.media.CreateVideoAssetResponse;
import vn.io.oldmoon.shopizer.product.app.dto.media.ImageAssetPopulator;
import vn.io.oldmoon.shopizer.product.app.dto.media.VideoAssetPopulator;
import vn.io.oldmoon.shopizer.product.business.service.ImageAssetService;
import vn.io.oldmoon.shopizer.product.business.service.VideoAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;

@WebMvcTest(controllers = AdminMediaController.class)
@ContextConfiguration(
    classes = {
      AdminMediaController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class AdminMediaControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ImageAssetService imageAssetService;
  @MockitoBean private ImageAssetPopulator imageAssetPopulator;
  @MockitoBean private VideoAssetService videoAssetService;
  @MockitoBean private VideoAssetPopulator videoAssetPopulator;

  private static final SimpleGrantedAuthority SUPER_ADMIN_AUTH =
      new SimpleGrantedAuthority("ROLE_SUPER_ADMIN");

  @Test
  @DisplayName("POST /api/v1/media/images with SUPER_ADMIN role should return 201 Created")
  void uploadImage_WithSuperAdminRole_ShouldReturn201() throws Exception {
    UUID assetId = UUID.randomUUID();
    MockMultipartFile imageFile =
        new MockMultipartFile("image", "gpu.jpg", "image/jpeg", "fake-image-bytes".getBytes());

    ImageAsset asset = ImageAsset.builder().build();
    asset.setId(assetId);
    CreateImageAssetResponse response = CreateImageAssetResponse.builder().id(assetId).build();

    given(imageAssetService.uploadImage(any(MultipartFile.class))).willReturn(asset);
    given(imageAssetPopulator.toDto(asset)).willReturn(response);

    mockMvc
        .perform(
            multipart("/api/v1/media/images")
                .file(imageFile)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(assetId.toString()));
  }

  @Test
  @DisplayName("POST /api/v1/media/images with empty file should return 400 Bad Request")
  void uploadImage_WithEmptyFile_ShouldReturn400() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

    mockMvc
        .perform(
            multipart("/api/v1/media/images")
                .file(emptyFile)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/media/images/{id} with SUPER_ADMIN role should return 200 OK")
  void getImage_WithSuperAdminRole_ShouldReturn200() throws Exception {
    UUID assetId = UUID.randomUUID();
    ImageAsset asset = ImageAsset.builder().build();
    asset.setId(assetId);
    CreateImageAssetResponse response = CreateImageAssetResponse.builder().id(assetId).build();

    given(imageAssetService.get(assetId)).willReturn(asset);
    given(imageAssetPopulator.toDto(asset)).willReturn(response);

    mockMvc
        .perform(
            get("/api/v1/media/images/{id}", assetId)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(assetId.toString()));
  }

  @Test
  @DisplayName("GET /api/v1/media/images/{id} when not found should return 404 Not Found")
  void getImage_WhenNotFound_ShouldReturn404() throws Exception {
    UUID assetId = UUID.randomUUID();
    given(imageAssetService.get(assetId))
        .willThrow(new ResourceNotFoundException("ImageAsset", assetId.toString()));

    mockMvc
        .perform(
            get("/api/v1/media/images/{id}", assetId)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /api/v1/media/images/{id} with SUPER_ADMIN role should return 204 No Content")
  void deleteImage_WithSuperAdminRole_ShouldReturn204() throws Exception {
    UUID assetId = UUID.randomUUID();

    mockMvc
        .perform(
            delete("/api/v1/media/images/{id}", assetId)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNoContent());

    verify(imageAssetService).delete(assetId);
  }

  @Test
  @DisplayName("POST /api/v1/media/videos with SUPER_ADMIN role should return 201 Created")
  void uploadVideo_WithSuperAdminRole_ShouldReturn201() throws Exception {
    UUID assetId = UUID.randomUUID();
    MockMultipartFile videoFile =
        new MockMultipartFile("video", "demo.mp4", "video/mp4", "fake-video-bytes".getBytes());

    VideoAsset asset = VideoAsset.builder().build();
    asset.setId(assetId);
    CreateVideoAssetResponse response = CreateVideoAssetResponse.builder().id(assetId).build();

    given(videoAssetService.uploadVideo(any(MultipartFile.class))).willReturn(asset);
    given(videoAssetPopulator.toDto(asset)).willReturn(response);

    mockMvc
        .perform(
            multipart("/api/v1/media/videos")
                .file(videoFile)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(assetId.toString()));
  }

  @Test
  @DisplayName("GET /api/v1/media/videos/{id} with SUPER_ADMIN role should return 200 OK")
  void getVideo_WithSuperAdminRole_ShouldReturn200() throws Exception {
    UUID assetId = UUID.randomUUID();
    VideoAsset asset = VideoAsset.builder().build();
    asset.setId(assetId);
    CreateVideoAssetResponse response = CreateVideoAssetResponse.builder().id(assetId).build();

    given(videoAssetService.get(assetId)).willReturn(asset);
    given(videoAssetPopulator.toDto(asset)).willReturn(response);

    mockMvc
        .perform(
            get("/api/v1/media/videos/{id}", assetId)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(assetId.toString()));
  }

  @Test
  @DisplayName("GET /api/v1/media/videos/{id} when not found should return 404 Not Found")
  void getVideo_WhenNotFound_ShouldReturn404() throws Exception {
    UUID assetId = UUID.randomUUID();
    given(videoAssetService.get(assetId))
        .willThrow(new ResourceNotFoundException("VideoAsset", assetId.toString()));

    mockMvc
        .perform(
            get("/api/v1/media/videos/{id}", assetId)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /api/v1/media/videos/{id} with SUPER_ADMIN role should return 204 No Content")
  void deleteVideo_WithSuperAdminRole_ShouldReturn204() throws Exception {
    UUID assetId = UUID.randomUUID();

    mockMvc
        .perform(
            delete("/api/v1/media/videos/{id}", assetId)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNoContent());

    verify(videoAssetService).delete(assetId);
  }
}
