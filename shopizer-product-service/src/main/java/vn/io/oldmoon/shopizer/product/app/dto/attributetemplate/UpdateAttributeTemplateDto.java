package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.app.dto.product.AttributeDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttributeTemplateDto {

  @Size(max = 255, message = "Template name must be 255 characters or fewer")
  @Schema(example = "GPU Specification Template Updated", description = "Attribute template name")
  private String name;

  @Size(max = 50, message = "Template code must be 50 characters or fewer")
  @Schema(example = "GPU_TEMPLATE_V2", description = "Unique code for the attribute template")
  private String code;

  @Valid
  @Schema(description = "List of attributes in the template")
  private List<AttributeDto> attributes;
}
