package vn.io.oldmoon.shopizer.product.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticlePopulator;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticleResponse;
import vn.io.oldmoon.shopizer.product.app.dto.article.CreateArticleRequest;
import vn.io.oldmoon.shopizer.product.app.dto.article.UpdateArticleRequest;
import vn.io.oldmoon.shopizer.product.business.service.ArticleService;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Article Endpoints", description = "Endpoints for managing articles")
public class AdminArticleController extends AbstractController {

  private final ArticleService articleService;
  private final ArticlePopulator articlePopulator;

  @PostMapping
  @Operation(
      summary = "Create article",
      description = "Creates a new article for computer component documentation and reviews")
  @ApiResponse(
      responseCode = "201",
      description = "Article created successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ArticleResponse.class)))
  public ResponseEntity<ArticleResponse> create(
      @Valid @RequestBody CreateArticleRequest createDto) {
    Article entity = articlePopulator.toEntity(createDto);
    Article created = articleService.create(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(articlePopulator.toDto(created));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update article",
      description = "Updates an existing article by its UUID")
  @ApiResponse(
      responseCode = "200",
      description = "Article updated successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ArticleResponse.class)))
  public ResponseEntity<ArticleResponse> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateArticleRequest updateDto) {
    Article existing = articleService.getById(id);
    articlePopulator.update(existing, updateDto);
    Article updated = articleService.update(existing);
    return ResponseEntity.ok(articlePopulator.toDto(updated));
  }
}
