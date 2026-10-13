package vn.io.oldmoon.shopizer.product.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.DuplicateResourceException;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;
import vn.io.oldmoon.shopizer.product.infra.repository.ArticleRepository;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleService articleService;

  @Nested
  @DisplayName("create(Article article)")
  class CreateTests {

    @Test
    @DisplayName("should throw NullPointerException when article is null")
    void create_WhenArticleIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> articleService.create(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Article must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when article slug is null")
    void create_WhenArticleSlugIsNull_ShouldThrowNpe() {
      Article article = Article.builder().title("RTX 4090 Review").slug(null).build();

      assertThatThrownBy(() -> articleService.create(article))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Article slug must not be null");
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when article slug already exists")
    void create_WhenSlugAlreadyExists_ShouldThrowDuplicateResourceException() {
      Article article =
          Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
      when(articleRepository.existsBySlug("rtx-4090-review")).thenReturn(true);

      assertThatThrownBy(() -> articleService.create(article))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Article with slug 'rtx-4090-review' already exists");
    }

    @Test
    @DisplayName("should save and return article when article is valid")
    void create_WhenValid_ShouldSaveAndReturnArticle() {
      Article article =
          Article.builder()
              .title("RTX 4090 Review")
              .slug("rtx-4090-review")
              .status(ArticleStatus.PUBLISHED)
              .build();
      when(articleRepository.existsBySlug("rtx-4090-review")).thenReturn(false);
      when(articleRepository.save(article)).thenReturn(article);

      Article result = articleService.create(article);

      assertThat(result).isNotNull().isEqualTo(article);
      verify(articleRepository).save(article);
    }
  }

  @Nested
  @DisplayName("getById(UUID id)")
  class GetByIdTests {

    @Test
    @DisplayName("should throw NullPointerException when id is null")
    void getById_WhenIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> articleService.getById(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("ID must not be null");
    }

    @Test
    @DisplayName("should return article when found by ID")
    void getById_WhenFound_ShouldReturnArticle() {
      UUID id = UUID.randomUUID();
      Article article =
          Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
      article.setId(id);
      when(articleRepository.findById(id)).thenReturn(Optional.of(article));

      Article result = articleService.getById(id);

      assertThat(result).isNotNull().isEqualTo(article);
      verify(articleRepository).findById(id);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when not found by ID")
    void getById_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(articleRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> articleService.getById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Article")
          .hasMessageContaining(id.toString());
    }
  }

  @Nested
  @DisplayName("getByIdOrSlug(String idOrSlug)")
  class GetByIdOrSlugTests {

    @Test
    @DisplayName("should throw InvalidInputException when identifier is null")
    void getByIdOrSlug_WhenNull_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> articleService.getByIdOrSlug(null))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Article identifier must not be blank");
    }

    @Test
    @DisplayName("should throw InvalidInputException when identifier is blank")
    void getByIdOrSlug_WhenBlank_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> articleService.getByIdOrSlug("   "))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Article identifier must not be blank");
    }

    @Test
    @DisplayName("should return article when found by UUID")
    void getByIdOrSlug_WhenFoundByUuid_ShouldReturnArticle() {
      UUID id = UUID.randomUUID();
      Article article =
          Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
      article.setId(id);
      when(articleRepository.findById(id)).thenReturn(Optional.of(article));

      Article result = articleService.getByIdOrSlug(id.toString());

      assertThat(result).isNotNull().isEqualTo(article);
      verify(articleRepository).findById(id);
    }

    @Test
    @DisplayName("should return article when found by exact slug")
    void getByIdOrSlug_WhenFoundBySlug_ShouldReturnArticle() {
      String slug = "rtx-4090-review";
      Article article = Article.builder().title("RTX 4090 Review").slug(slug).build();
      when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));

      Article result = articleService.getByIdOrSlug(slug);

      assertThat(result).isNotNull().isEqualTo(article);
      verify(articleRepository).findBySlug(slug);
    }

    @Test
    @DisplayName("should return article when found by slug ignore case")
    void getByIdOrSlug_WhenFoundBySlugIgnoreCase_ShouldReturnArticle() {
      String slug = "RTX-4090-REVIEW";
      Article article =
          Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
      when(articleRepository.findBySlug(slug)).thenReturn(Optional.empty());
      when(articleRepository.findBySlugIgnoreCase(slug)).thenReturn(Optional.of(article));

      Article result = articleService.getByIdOrSlug(slug);

      assertThat(result).isNotNull().isEqualTo(article);
      verify(articleRepository).findBySlugIgnoreCase(slug);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when article is not found by ID or slug")
    void getByIdOrSlug_WhenNotFound_ShouldThrowResourceNotFoundException() {
      String slug = "non-existent-slug";
      when(articleRepository.findBySlug(slug)).thenReturn(Optional.empty());
      when(articleRepository.findBySlugIgnoreCase(slug)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> articleService.getByIdOrSlug(slug))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Article")
          .hasMessageContaining(slug);
    }
  }

  @Nested
  @DisplayName("update(Article article)")
  class UpdateTests {

    @Test
    @DisplayName("should throw NullPointerException when article is null")
    void update_WhenArticleIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> articleService.update(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Article must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when article slug is null")
    void update_WhenSlugIsNull_ShouldThrowNpe() {
      Article article = Article.builder().title("RTX 4090 Review").slug(null).build();
      article.setId(UUID.randomUUID());

      assertThatThrownBy(() -> articleService.update(article))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Article slug must not be null");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when article id is null")
    void update_WhenIdIsNull_ShouldThrowResourceNotFoundException() {
      Article article =
          Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();

      assertThatThrownBy(() -> articleService.update(article))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Article");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when article does not exist")
    void update_WhenArticleDoesNotExist_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      Article article =
          Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
      article.setId(id);
      when(articleRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> articleService.update(article))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Article")
          .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when changed slug already exists")
    void update_WhenChangedSlugAlreadyExists_ShouldThrowDuplicateResourceException() {
      UUID id = UUID.randomUUID();
      Article existing =
          Article.builder().title("Old Review").slug("old-review").build();
      existing.setId(id);

      Article updated =
          Article.builder().title("Updated Review").slug("new-review").build();
      updated.setId(id);

      when(articleRepository.findById(id)).thenReturn(Optional.of(existing));
      when(articleRepository.existsBySlug("new-review")).thenReturn(true);

      assertThatThrownBy(() -> articleService.update(updated))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Article with slug 'new-review' already exists");
    }

    @Test
    @DisplayName("should save and return updated article when slug is unchanged")
    void update_WhenSlugUnchanged_ShouldSaveAndReturnArticle() {
      UUID id = UUID.randomUUID();
      Article existing =
          Article.builder().title("Old Review").slug("same-slug").build();
      existing.setId(id);

      Article updated =
          Article.builder().title("Updated Review").slug("same-slug").build();
      updated.setId(id);

      when(articleRepository.findById(id)).thenReturn(Optional.of(existing));
      when(articleRepository.save(updated)).thenReturn(updated);

      Article result = articleService.update(updated);

      assertThat(result).isNotNull().isEqualTo(updated);
      verify(articleRepository).save(updated);
    }

    @Test
    @DisplayName("should save and return updated article when changed slug does not exist")
    void update_WhenChangedSlugIsUnique_ShouldSaveAndReturnArticle() {
      UUID id = UUID.randomUUID();
      Article existing =
          Article.builder().title("Old Review").slug("old-slug").build();
      existing.setId(id);

      Article updated =
          Article.builder().title("Updated Review").slug("new-unique-slug").build();
      updated.setId(id);

      when(articleRepository.findById(id)).thenReturn(Optional.of(existing));
      when(articleRepository.existsBySlug("new-unique-slug")).thenReturn(false);
      when(articleRepository.save(updated)).thenReturn(updated);

      Article result = articleService.update(updated);

      assertThat(result).isNotNull().isEqualTo(updated);
      verify(articleRepository).save(updated);
    }
  }
}
