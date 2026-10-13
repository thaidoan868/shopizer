package vn.io.oldmoon.shopizer.product.app.dto.article;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

class ArticleMapperTest {

  private final ArticleMapper articleMapper = Mappers.getMapper(ArticleMapper.class);

  @Test
  @DisplayName("toEntity should return null when dto is null")
  void toEntity_WhenDtoIsNull_ShouldReturnNull() {
    assertThat(articleMapper.toEntity(null)).isNull();
  }

  @Test
  @DisplayName("toEntity should map valid dto to entity")
  void toEntity_WhenDtoIsValid_ShouldMapToEntity() {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("RTX 4090 Review")
            .slug("rtx-4090-review")
            .contentHtml("<p>Review content</p>")
            .status(ArticleStatus.PUBLISHED)
            .build();

    Article article = articleMapper.toEntity(request);

    assertThat(article).isNotNull();
    assertThat(article.getTitle()).isEqualTo("RTX 4090 Review");
    assertThat(article.getSlug()).isEqualTo("rtx-4090-review");
    assertThat(article.getContentHtml()).isEqualTo("<p>Review content</p>");
    assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
  }

  @Test
  @DisplayName("toDto should return null when article is null")
  void toDto_WhenArticleIsNull_ShouldReturnNull() {
    assertThat(articleMapper.toDto(null)).isNull();
  }

  @Test
  @DisplayName("toDto should map valid article entity to ArticleResponse")
  void toDto_WhenArticleIsValid_ShouldMapToDto() {
    UUID id = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();
    UUID modifiedBy = UUID.randomUUID();
    Instant now = Instant.now();

    Article article =
        Article.builder()
            .title("RTX 4090 Review")
            .slug("rtx-4090-review")
            .contentHtml("<p>Review content</p>")
            .status(ArticleStatus.PUBLISHED)
            .build();
    article.setId(id);
    article.setCreated(now);
    article.setCreatedBy(createdBy);
    article.setModified(now);
    article.setModifiedBy(modifiedBy);

    ArticleResponse response = articleMapper.toDto(article);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getTitle()).isEqualTo("RTX 4090 Review");
    assertThat(response.getSlug()).isEqualTo("rtx-4090-review");
    assertThat(response.getContentHtml()).isEqualTo("<p>Review content</p>");
    assertThat(response.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
    assertThat(response.getCreated()).isEqualTo(now);
    assertThat(response.getCreatedBy()).isEqualTo(createdBy);
    assertThat(response.getModified()).isEqualTo(now);
    assertThat(response.getModifiedBy()).isEqualTo(modifiedBy);
  }

  @Test
  @DisplayName("updateEntityFromDto should update non-null fields on entity")
  void updateEntityFromDto_WhenDtoIsValid_ShouldUpdateEntity() {
    Article article =
        Article.builder()
            .title("Old Title")
            .slug("old-slug")
            .contentHtml("<p>Old</p>")
            .status(ArticleStatus.DRAFT)
            .build();

    UpdateArticleRequest dto =
        UpdateArticleRequest.builder()
            .title("New Title")
            .slug("new-slug")
            .contentHtml("<p>New</p>")
            .status(ArticleStatus.PUBLISHED)
            .build();

    articleMapper.updateEntityFromDto(dto, article);

    assertThat(article.getTitle()).isEqualTo("New Title");
    assertThat(article.getSlug()).isEqualTo("new-slug");
    assertThat(article.getContentHtml()).isEqualTo("<p>New</p>");
    assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
  }
}
