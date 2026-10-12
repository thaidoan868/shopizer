package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.app.dto.product.AttributeDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeTemplateResponse {

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "Unique identifier of the attribute template")
  private UUID id;

  @Schema(example = "Graphics Card Attributes", description = "Attribute template name")
  private String name;

  @Schema(example = "GPU_TEMPLATE", description = "Unique code for the attribute template")
  private String code;

  @Schema(description = "List of attributes in the template")
  private List<AttributeDto> attributes;
}
