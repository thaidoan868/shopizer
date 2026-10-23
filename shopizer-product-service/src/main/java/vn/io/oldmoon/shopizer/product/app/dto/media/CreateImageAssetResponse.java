package vn.io.oldmoon.shopizer.product.app.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageAssetResponse {

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "Unique identifier of the image asset")
  private UUID id;

  @Schema(description = "Full metadata and URL for the original image")
  private FileResponse original;

  @Schema(
      description =
          "Full metadata and URL for the resized thumbnail image (200px max edge, EXIF stripped)")
  private FileResponse thumbnail;

  @Schema(
      description =
          "Full metadata and URL for the resized medium image (800px max edge, EXIF stripped)")
  private FileResponse medium;

  @Schema(
      description =
          "Full metadata and URL for the resized large image (1600px max edge, EXIF stripped)")
  private FileResponse large;
}
