package vn.io.oldmoon.shopizer.product.app.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {

  @Schema(example = "AVAILABLE", description = "Processing status of the video variation")
  private VideoStatus status;

  @Schema(description = "File metadata and URL when the video variation is available")
  private FileResponse file;
}
