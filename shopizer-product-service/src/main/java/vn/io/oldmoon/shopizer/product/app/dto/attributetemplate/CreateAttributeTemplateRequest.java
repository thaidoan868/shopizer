package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class CreateAttributeTemplateRequest {

  @NotBlank(message = "Template name must not be blank")
  @Size(max = 255, message = "Template name must be 255 characters or fewer")
  @Schema(
      example = "GPU Specification Template",
      description = "Attribute template name",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotBlank(message = "Template code must not be blank")
  @Size(max = 50, message = "Template code must be 50 characters or fewer")
  @Schema(
      example = "GPU_TEMPLATE",
      description = "Unique code for the attribute template",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String code;

  @Valid
  @Schema(description = "List of attributes in the template")
  private List<AttributeDto> attributes;
}
