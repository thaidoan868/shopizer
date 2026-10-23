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
public class CreateVideoAssetResponse {

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "Unique identifier of the video asset")
  private UUID id;

  @Schema(description = "Source video status and metadata")
  private VideoResponse original;

  @Schema(description = "360p resolution variation status and metadata")
  private VideoResponse p360;

  @Schema(description = "720p resolution variation status and metadata")
  private VideoResponse p720;

  @Schema(description = "1080p resolution variation status and metadata")
  private VideoResponse p1080;
}
