package vn.io.oldmoon.shopizer.product.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticlePopulator;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticleResponse;
import vn.io.oldmoon.shopizer.product.business.service.ArticleService;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;

@RestController
@RequestMapping("/api/v1/public/articles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Article Endpoints", description = "Endpoints for managing articles")
public class PublicArticleController extends AbstractController {

  private final ArticleService articleService;
  private final ArticlePopulator articlePopulator;

  @GetMapping("/{idOrSlug}")
  @Operation(
      summary = "Get article by ID or slug",
      description = "Retrieves an article using either its UUID or its unique URL slug")
  @ApiResponse(
      responseCode = "200",
      description = "Article retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ArticleResponse.class)))
  public ResponseEntity<ArticleResponse> getByIdOrSlug(@PathVariable("idOrSlug") String idOrSlug) {
    Article article = articleService.getByIdOrSlug(idOrSlug);
    return ResponseEntity.ok(articlePopulator.toDto(article));
  }
}
