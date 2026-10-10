package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
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
public class AttributeTemplateDto {

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

  @Schema(example = "2026-09-25T09:00:00Z", description = "Creation timestamp")
  private Instant created;

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "ID of the user who created the template")
  private UUID createdBy;

  @Schema(example = "2026-09-25T09:00:00Z", description = "Last modification timestamp")
  private Instant modified;

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "ID of the user who last modified the template")
  private UUID modifiedBy;
}
