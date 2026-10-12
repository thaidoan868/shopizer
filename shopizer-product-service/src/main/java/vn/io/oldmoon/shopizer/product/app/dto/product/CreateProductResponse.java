package vn.io.oldmoon.shopizer.product.app.dto.product;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.infra.model.product.ProductStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductResponse {

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "Unique identifier of the product")
  private UUID id;

  @Schema(example = "NVIDIA GeForce RTX 3050", description = "Product name")
  private String name;

  @Schema(example = "NVIDIA-GeForce-RTX-3050-a1b2c3d4", description = "Unique slug of the product")
  private String slug;

  @Schema(example = "NVIDIA", description = "Product brand")
  private String brand;

  @Schema(
      example =
          "Budget-friendly gaming graphics card featuring Ampere architecture and ray tracing.",
      description = "Product description")
  private String description;

  @Schema(description = "List of product attributes")
  private List<AttributeDto> attributes;

  @JsonAlias({"isSerialized", "is_serialized"})
  @Schema(example = "true", description = "Whether the product is serialized")
  private Boolean serialized;

  @Schema(example = "ACTIVE", description = "Product status")
  private ProductStatus status;

  @Schema(example = "2026-09-25T09:00:00Z", description = "Creation timestamp")
  private Instant created;

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "ID of the user who created the product")
  private UUID createdBy;

  @Schema(example = "2026-09-25T09:00:00Z", description = "Last modification timestamp")
  private Instant modified;

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "ID of the user who last modified the product")
  private UUID modifiedBy;
}
