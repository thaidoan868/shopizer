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
import vn.io.oldmoon.shopizer.common.core.utility.ImageUtil;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.media.CreateImageAssetResponse;
import vn.io.oldmoon.shopizer.product.app.dto.media.ImageAssetPopulator;
import vn.io.oldmoon.shopizer.product.business.service.ImageAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/media/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Endpoints", description = "Endpoints for managing media and image uploads")
public class AdminMediaController extends AbstractController {

  private final ImageAssetService imageAssetService;
  private final ImageAssetPopulator imageAssetPopulator;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload multiple images",
      description =
          "Uploads multiple images, generating thumbnail (200px), medium (800px), and large (1600px) variants with EXIF stripped")
  @ApiResponse(
      responseCode = "201",
      description = "Images uploaded successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              array =
                  @ArraySchema(schema = @Schema(implementation = CreateImageAssetResponse.class))))
  public ResponseEntity<List<CreateImageAssetResponse>> uploadImages(
      @RequestParam(value = "images", required = false) List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      throw new InvalidInputException("Image files must not be empty");
    }

    images.forEach(ImageUtil::validateBasic);

    List<ImageAsset> assets = imageAssetService.uploadImages(images);
    List<CreateImageAssetResponse> responses = imageAssetPopulator.toDtoList(assets);
    return ResponseEntity.status(HttpStatus.CREATED).body(responses);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete image asset",
      description = "Deletes an image asset by soft-deleting its associated file records")
  @ApiResponse(responseCode = "204", description = "Image asset deleted successfully")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    imageAssetService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
