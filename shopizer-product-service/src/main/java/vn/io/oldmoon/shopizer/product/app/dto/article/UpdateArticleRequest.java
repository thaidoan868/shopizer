package vn.io.oldmoon.shopizer.product.app.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class UpdateArticleRequest {

  @Size(max = 255, message = "Article title must be 255 characters or fewer")
  @Schema(
      example = "NVIDIA GeForce RTX 4090: Thermal and Power Benchmarks",
      description = "Article title")
  private String title;

  @Size(max = 255, message = "Article slug must be 255 characters or fewer")
  @Schema(
      example = "nvidia-geforce-rtx-4090-thermal-benchmarks",
      description = "Custom URL slug for the article")
  private String slug;

  @WellFormedHtml
  @Schema(
      example =
          "<p>Updated thermal results: Under full FurMark load paired with an Intel Core i9-14900K,"
              + " the GPU peaks at 68 degrees Celsius.</p>",
      description = "Article body in well-formed HTML format")
  private String contentHtml;

  @Schema(
      example = "PUBLISHED",
      description = "Article publishing status (DRAFT, PUBLISHED, ARCHIVED)")
  private ArticleStatus status;
}
