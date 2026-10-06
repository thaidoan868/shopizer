package vn.io.oldmoon.shopizer.product.app.dto.product;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.infra.model.product.ProductStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDto {

  @NotBlank(message = "Product name must not be blank")
  @Size(max = 255, message = "Product name must be 255 characters or fewer")
  @Schema(
      example = "NVIDIA GeForce RTX 3050",
      description = "Product name",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @Size(max = 255, message = "Slug must be 255 characters or fewer")
  @Schema(
      example = "nvidia-geforce-rtx-3050",
      description =
          "Product URL slug. If not provided, it will be automatically generated from the product name.")
  private String slug;

  @Size(max = 100, message = "Brand must be 100 characters or fewer")
  @Schema(example = "NVIDIA", description = "Product brand")
  private String brand;

  @Schema(
      example =
          "Budget-friendly gaming graphics card featuring Ampere architecture and ray tracing.",
      description = "Product description")
  private String description;

  @Valid
  @Schema(description = "List of product attributes")
  private List<AttributeDto> attributes;

  @JsonAlias({"isSerialized", "is_serialized"})
  @Schema(
      example = "true",
      description = "Whether the product is serialized (tracked by individual serial numbers)",
      defaultValue = "false")
  private Boolean serialized;

  @Schema(example = "ACTIVE", description = "Product status", defaultValue = "ACTIVE")
  private ProductStatus status;
}
