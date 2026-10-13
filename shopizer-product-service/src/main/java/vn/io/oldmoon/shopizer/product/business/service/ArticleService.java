package vn.io.oldmoon.shopizer.product.business.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.DuplicateResourceException;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.repository.ArticleRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ArticleService {

  private final ArticleRepository articleRepository;

  /**
   * Persists a new article.
   *
   * @param article the Article domain entity to persist
   * @return the persisted Article domain entity
   * @throws DuplicateResourceException if an article with the same slug already exists
   */
  @Transactional
  public Article create(Article article) {
    Objects.requireNonNull(article, "Article must not be null");
    Objects.requireNonNull(article.getSlug(), "Article slug must not be null");

    if (articleRepository.existsBySlug(article.getSlug())) {
      throw new DuplicateResourceException(
          String.format("Article with slug '%s' already exists", article.getSlug()));
    }

    log.info("Creating article with slug='{}'", article.getSlug());
    return articleRepository.save(article);
  }

  /**
   * Retrieves an article by its UUID.
   *
   * @param id the UUID of the article
   * @return the Article domain entity
   * @throws ResourceNotFoundException if no article is found
   */
  public Article getById(UUID id) {
    Objects.requireNonNull(id, "ID must not be null");
    log.info("Fetching article with id='{}'", id);
    return articleRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Article", id.toString()));
  }

  /**
   * Retrieves an article by either its UUID or its slug.
   *
   * @param idOrSlug UUID string or slug
   * @return the Article domain entity
   * @throws InvalidInputException if the identifier is blank
   * @throws ResourceNotFoundException if no matching article is found
   */
  public Article getByIdOrSlug(String idOrSlug) {
    if (idOrSlug == null || idOrSlug.isBlank()) {
      throw new InvalidInputException("Article identifier must not be blank");
    }

    String trimmed = idOrSlug.trim();
    Optional<Article> articleOpt = Optional.empty();

    try {
      UUID id = UUID.fromString(trimmed);
      articleOpt = articleRepository.findById(id);
    } catch (IllegalArgumentException ignored) {
      // Not a valid UUID format, look up by slug
    }

    Article article =
        articleOpt
            .or(() -> articleRepository.findBySlug(trimmed))
            .or(() -> articleRepository.findBySlugIgnoreCase(trimmed))
            .orElseThrow(() -> new ResourceNotFoundException("Article", idOrSlug));

    log.info(
        "Found article id='{}', slug='{}' for identifier='{}'",
        article.getId(),
        article.getSlug(),
        idOrSlug);
    return article;
  }

  /**
   * Updates an existing article.
   *
   * @param article the updated Article domain entity
   * @return the saved Article domain entity
   * @throws ResourceNotFoundException if the article does not exist
   * @throws DuplicateResourceException if the updated slug already exists
   */
  @Transactional
  public Article update(Article article) {
    Objects.requireNonNull(article, "Article must not be null");
    Objects.requireNonNull(article.getSlug(), "Article slug must not be null");

    if (article.getId() == null) {
      throw new ResourceNotFoundException("Article", "null");
    }

    Article existing =
        articleRepository
            .findById(article.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Article", article.getId().toString()));

    boolean slugChanged = !article.getSlug().equalsIgnoreCase(existing.getSlug());
    if (slugChanged && articleRepository.existsBySlug(article.getSlug())) {
      throw new DuplicateResourceException(
          String.format("Article with slug '%s' already exists", article.getSlug()));
    }

    log.info("Updating article with id='{}'", article.getId());
    return articleRepository.save(article);
  }

}
