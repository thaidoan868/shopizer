package vn.io.oldmoon.shopizer.product.app.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440000",
      description = "Unique identifier of the article")
  private UUID id;

  @Schema(
      example = "NVIDIA GeForce RTX 4090: Architecture and Gaming Performance Review",
      description = "Article title")
  private String title;

  @Schema(
      example = "nvidia-geforce-rtx-4090-review",
      description = "Unique URL slug for the article")
  private String slug;

  @Schema(
      example =
          "<p>The <strong>NVIDIA GeForce RTX 4090</strong> delivers unprecedented performance for"
              + " 4K gaming, powered by the Ada Lovelace architecture with 24GB GDDR6X"
              + " memory.</p>",
      description = "Sanitized article body in HTML format")
  private String contentHtml;

  @Schema(example = "PUBLISHED", description = "Article publishing status")
  private ArticleStatus status;

  @Schema(example = "2026-10-13T10:00:00Z", description = "Creation timestamp")
  private Instant created;

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440001",
      description = "Identifier of the user who created the article")
  private UUID createdBy;

  @Schema(example = "2026-10-13T10:00:00Z", description = "Last modification timestamp")
  private Instant modified;

  @Schema(
      example = "550e8400-e29b-41d4-a716-446655440001",
      description = "Identifier of the user who last modified the article")
  private UUID modifiedBy;
}
