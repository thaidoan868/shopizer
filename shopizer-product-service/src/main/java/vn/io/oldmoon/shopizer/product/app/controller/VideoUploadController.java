package vn.io.oldmoon.shopizer.product.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.utility.VideoUtil;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.media.CreateVideoAssetResponse;
import vn.io.oldmoon.shopizer.product.app.dto.media.VideoAssetPopulator;
import vn.io.oldmoon.shopizer.product.business.service.VideoAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/media/videos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Endpoints", description = "Endpoints for managing video uploads and processing")
public class VideoUploadController extends AbstractController {

  private final VideoAssetService videoAssetService;
  private final VideoAssetPopulator videoAssetPopulator;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload multiple videos",
      description =
          "Uploads original videos to MinIO, initializes VideoAsset records, and queues asynchronous 360p, 720p, and 1080p transcoding tasks")
  @ApiResponse(
      responseCode = "201",
      description = "Videos uploaded and queued for processing",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              array =
                  @ArraySchema(schema = @Schema(implementation = CreateVideoAssetResponse.class))))
  public ResponseEntity<List<CreateVideoAssetResponse>> uploadVideos(
      @RequestParam(value = "videos", required = false) List<MultipartFile> videos) {
    if (videos == null || videos.isEmpty()) {
      throw new InvalidInputException("Video files must not be empty");
    }

    videos.forEach(VideoUtil::validateBasic);

    List<VideoAsset> assets = videoAssetService.uploadVideos(videos);
    List<CreateVideoAssetResponse> responses = videoAssetPopulator.toDtoList(assets);
    return ResponseEntity.status(HttpStatus.CREATED).body(responses);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete video asset",
      description =
          "Soft deletes a video asset by marking its associated file records as DELETED and variations as NOT_AVAILABLE")
  @ApiResponse(responseCode = "204", description = "Video asset deleted successfully")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    videoAssetService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
