package vn.io.oldmoon.shopizer.product.app.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.oldmoon.shopizer.common.core.validation.WellFormedHtml;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleRequest {

  @NotBlank(message = "Article title must not be blank")
  @Size(max = 255, message = "Article title must be 255 characters or fewer")
  @Schema(
      example = "NVIDIA GeForce RTX 4090: Architecture and Gaming Performance Review",
      description = "Article title",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Size(max = 255, message = "Article slug must be 255 characters or fewer")
  @Schema(
      example = "nvidia-geforce-rtx-4090-review",
      description = "Custom URL slug for the article. Auto-generated if not provided.")
  private String slug;

  @WellFormedHtml
  @Schema(
      example =
          "<p>The <strong>NVIDIA GeForce RTX 4090</strong> delivers unprecedented performance for"
              + " 4K gaming, powered by the Ada Lovelace architecture with 24GB GDDR6X"
              + " memory.</p>",
      description = "Article body in well-formed HTML format")
  private String contentHtml;

  @Schema(
      example = "PUBLISHED",
      description = "Article publishing status (DRAFT, PUBLISHED, ARCHIVED). Defaults to DRAFT.")
  private ArticleStatus status;
}
