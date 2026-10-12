package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortAttributeTemplateResponse {

  @Schema(example = "Graphics Card Attributes", description = "Attribute template name")
  private String name;

  @Schema(example = "GPU_TEMPLATE", description = "Unique code for the attribute template")
  private String code;
}
