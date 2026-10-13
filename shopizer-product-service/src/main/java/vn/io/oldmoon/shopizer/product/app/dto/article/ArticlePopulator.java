package vn.io.oldmoon.shopizer.product.app.dto.article;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.utility.HtmlSanitizerUtil;
import vn.io.oldmoon.shopizer.common.core.utility.SlugUtil;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticlePopulator {

  private final ArticleMapper articleMapper;

  /**
   * Converts CreateArticleRequest to an Article domain entity.
   * Auto-generates slug if missing, sets default status to DRAFT, and sanitizes HTML content.
   *
   * @param dto the incoming CreateArticleRequest
   * @return the populated Article domain entity
   */
  public Article toEntity(CreateArticleRequest dto) {
    Objects.requireNonNull(dto, "CreateArticleRequest must not be null");

    Article article = articleMapper.toEntity(dto);

    article.setSlug(SlugUtil.generateSlug(dto.getSlug(), dto.getTitle()));

    if (article.getStatus() == null) {
      article.setStatus(ArticleStatus.DRAFT);
    }

    if (dto.getContentHtml() != null) {
      String sanitized = HtmlSanitizerUtil.sanitize(dto.getContentHtml());
      article.setContentHtml(sanitized);
    }

    log.debug("Mapped CreateArticleRequest to Article for title={}", article.getTitle());

    return article;
  }

  /**
   * Updates an existing Article domain entity from UpdateArticleRequest.
   * Sanitizes HTML content and normalizes slug if provided.
   *
   * @param article the target Article domain entity
   * @param dto the incoming UpdateArticleRequest
   */
  public void update(Article article, UpdateArticleRequest dto) {
    Objects.requireNonNull(article, "Article must not be null");
    Objects.requireNonNull(dto, "UpdateArticleRequest must not be null");

    articleMapper.updateEntityFromDto(dto, article);

    if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
      article.setSlug(dto.getSlug().trim().toLowerCase());
    }

    if (dto.getContentHtml() != null) {
      String sanitized = HtmlSanitizerUtil.sanitize(dto.getContentHtml());
      article.setContentHtml(sanitized);
    }

    log.debug("Updated Article for id={}", article.getId());
  }

  /**
   * Converts an Article domain entity to an ArticleResponse.
   *
   * @param article the Article domain entity
   * @return the corresponding ArticleResponse
   */
  public ArticleResponse toDto(Article article) {
    Objects.requireNonNull(article, "Article must not be null");

    ArticleResponse dto = articleMapper.toDto(article);
    log.debug("Mapped Article to ArticleResponse: id='{}'", article.getId());
    return dto;
  }
}
