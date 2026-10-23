package vn.io.oldmoon.shopizer.product.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.utility.ImageUtil;
import vn.io.oldmoon.shopizer.common.core.utility.VideoUtil;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.media.CreateImageAssetResponse;
import vn.io.oldmoon.shopizer.product.app.dto.media.CreateVideoAssetResponse;
import vn.io.oldmoon.shopizer.product.app.dto.media.ImageAssetPopulator;
import vn.io.oldmoon.shopizer.product.app.dto.media.VideoAssetPopulator;
import vn.io.oldmoon.shopizer.product.business.service.ImageAssetService;
import vn.io.oldmoon.shopizer.product.business.service.VideoAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Media Endpoints",
    description = "Endpoints for managing image and video uploads and processing")
public class AdminMediaController extends AbstractController {

  private final ImageAssetService imageAssetService;
  private final ImageAssetPopulator imageAssetPopulator;
  private final VideoAssetService videoAssetService;
  private final VideoAssetPopulator videoAssetPopulator;

  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload image",
      description =
          "Uploads an image, generating thumbnail (200px), medium (800px), and large (1600px) variants with EXIF stripped")
  @ApiResponse(
      responseCode = "201",
      description = "Image uploaded successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateImageAssetResponse.class)))
  public ResponseEntity<CreateImageAssetResponse> uploadImage(
      @RequestParam(value = "image", required = false) MultipartFile image) {
    ImageUtil.validateBasic(image);

    ImageAsset asset = imageAssetService.uploadImage(image);
    CreateImageAssetResponse response = imageAssetPopulator.toDto(asset);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/images/{id}")
  @Operation(
      summary = "Get image asset by ID",
      description = "Retrieves an image asset and its resolution variations by UUID")
  @ApiResponse(
      responseCode = "200",
      description = "Image asset retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateImageAssetResponse.class)))
  public ResponseEntity<CreateImageAssetResponse> getImage(@PathVariable("id") UUID id) {
    ImageAsset asset = imageAssetService.get(id);
    CreateImageAssetResponse response = imageAssetPopulator.toDto(asset);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/images/{id}")
  @Operation(
      summary = "Delete image asset",
      description = "Deletes an image asset and removes its associated files from storage")
  @ApiResponse(responseCode = "204", description = "Image asset deleted successfully")
  public ResponseEntity<Void> deleteImage(@PathVariable("id") UUID id) {
    imageAssetService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload video",
      description =
          "Uploads an original video to MinIO, initializes a VideoAsset record, and queues asynchronous 360p, 720p, and 1080p transcoding tasks")
  @ApiResponse(
      responseCode = "201",
      description = "Video uploaded and queued for processing",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateVideoAssetResponse.class)))
  public ResponseEntity<CreateVideoAssetResponse> uploadVideo(
      @RequestParam(value = "video", required = false) MultipartFile video) {
    VideoUtil.validateBasic(video);

    VideoAsset asset = videoAssetService.uploadVideo(video);
    CreateVideoAssetResponse response = videoAssetPopulator.toDto(asset);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/videos/{id}")
  @Operation(
      summary = "Get video asset by ID",
      description = "Retrieves a video asset and its resolution variations by UUID")
  @ApiResponse(
      responseCode = "200",
      description = "Video asset retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateVideoAssetResponse.class)))
  public ResponseEntity<CreateVideoAssetResponse> getVideo(@PathVariable("id") UUID id) {
    VideoAsset asset = videoAssetService.get(id);
    CreateVideoAssetResponse response = videoAssetPopulator.toDto(asset);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/videos/{id}")
  @Operation(
      summary = "Delete video asset",
      description = "Deletes a video asset and removes its associated files from storage")
  @ApiResponse(responseCode = "204", description = "Video asset deleted successfully")
  public ResponseEntity<Void> deleteVideo(@PathVariable("id") UUID id) {
    videoAssetService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
