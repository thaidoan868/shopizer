package vn.io.oldmoon.shopizer.product.app.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Visibility;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "Unique identifier of the file")
  private UUID id;

  @Schema(
      example = "http://localhost:9000/media-public/image-original-550e8400-rtx4090.jpg",
      description = "Publicly accessible URL of the image")
  private String url;

  @Schema(example = "media-public", description = "Storage bucket name")
  private String bucket;

  @Schema(example = "image-original-550e8400-rtx4090.jpg", description = "Object storage name")
  private String objectName;

  @Schema(example = "2458120", description = "File size in bytes")
  private Long sizeBytes;

  @Schema(example = "image/jpeg", description = "MIME content type")
  private String contentType;

  @Schema(example = "PUBLIC", description = "File visibility")
  private Visibility visibility;

  @Schema(example = "ACTIVE", description = "File status")
  private FileStatus status;
}
