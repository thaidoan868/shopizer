package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeRequest {

  @NotBlank(message = "Attribute name is required")
  @Schema(
      example = "VRAM",
      description = "Name of the attribute",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @Schema(
      example = "8GB GDDR6",
      description = "Value of the attribute",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String value;
}
