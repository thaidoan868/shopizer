package vn.io.oldmoon.shopizer.product.infra.model.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {

  private VideoStatus status;
  private Double duration;
  private File file;
}
