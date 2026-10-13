package vn.io.oldmoon.shopizer.product.app.dto.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

@ExtendWith(MockitoExtension.class)
class ArticlePopulatorTest {

  @Mock private ArticleMapper articleMapper;

  @InjectMocks private ArticlePopulator articlePopulator;

  @Nested
  @DisplayName("toEntity(CreateArticleRequest dto)")
  class ToEntityTests {

    @Test
    @DisplayName("should throw NullPointerException when dto is null")
    void toEntity_WhenDtoIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> articlePopulator.toEntity(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("CreateArticleRequest must not be null");
    }

    @Test
    @DisplayName("should populate entity with auto-generated slug and default status DRAFT")
    void toEntity_WhenSlugAndStatusNull_ShouldSetDefaults() {
      CreateArticleRequest dto =
          CreateArticleRequest.builder().title("RTX 4090 Review").build();
      Article mappedEntity = Article.builder().title("RTX 4090 Review").status(null).build();

      when(articleMapper.toEntity(dto)).thenReturn(mappedEntity);

      Article result = articlePopulator.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getSlug()).startsWith("rtx-4090-review-");
      assertThat(result.getStatus()).isEqualTo(ArticleStatus.DRAFT);
    }

    @Test
    @DisplayName("should lowercase explicit slug and preserve provided status")
    void toEntity_WhenExplicitSlugAndStatusProvided_ShouldUseThem() {
      CreateArticleRequest dto =
          CreateArticleRequest.builder()
              .title("RTX 4090 Review")
              .slug("RTX-4090-REVIEW")
              .status(ArticleStatus.PUBLISHED)
              .build();
      Article mappedEntity =
          Article.builder()
              .title("RTX 4090 Review")
              .status(ArticleStatus.PUBLISHED)
              .build();

      when(articleMapper.toEntity(dto)).thenReturn(mappedEntity);

      Article result = articlePopulator.toEntity(dto);

      assertThat(result.getSlug()).isEqualTo("rtx-4090-review");
      assertThat(result.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
    }

    @Test
    @DisplayName("should sanitize WYSIWYG HTML content preserving rich formatting and stripping scripts")
    void toEntity_WithWysiwygHtmlContent_ShouldSanitizeCorrectly() {
      String wysiwygHtml =
          "<h2>Architecture Overview</h2>"
              + "<p>The <strong>RTX 4090</strong> features 16,384 CUDA cores. "
              + "Read more at <a href=\"https://nvidia.com\">NVIDIA</a>.</p>"
              + "<ul><li>24GB GDDR6X</li><li>450W TDP</li></ul>"
              + "<script>alert('malicious')</script>"
              + "<img src=\"https://example.com/rtx4090.jpg\" alt=\"RTX 4090\" onerror=\"alert(1)\" />";

      CreateArticleRequest dto =
          CreateArticleRequest.builder()
              .title("RTX 4090 Review")
              .slug("rtx-4090-review")
              .contentHtml(wysiwygHtml)
              .status(ArticleStatus.PUBLISHED)
              .build();

      Article mappedEntity =
          Article.builder().title("RTX 4090 Review").contentHtml(wysiwygHtml).build();

      when(articleMapper.toEntity(dto)).thenReturn(mappedEntity);

      Article result = articlePopulator.toEntity(dto);

      assertThat(result.getContentHtml()).contains("<h2>Architecture Overview</h2>");
      assertThat(result.getContentHtml()).contains("<strong>RTX 4090</strong>");
      assertThat(result.getContentHtml()).contains("<ul><li>24GB GDDR6X</li><li>450W TDP</li></ul>");
      assertThat(result.getContentHtml()).contains("<a href=\"https://nvidia.com\" rel=\"nofollow\">NVIDIA</a>");
      assertThat(result.getContentHtml()).contains("<img src=\"https://example.com/rtx4090.jpg\" alt=\"RTX 4090\" />");
      assertThat(result.getContentHtml()).doesNotContain("<script>");
      assertThat(result.getContentHtml()).doesNotContain("onerror");
    }
  }

  @Nested
  @DisplayName("update(Article article, UpdateArticleRequest dto)")
  class UpdateTests {

    @Test
    @DisplayName("should throw NullPointerException when article is null")
    void update_WhenArticleIsNull_ShouldThrowNpe() {
      UpdateArticleRequest dto = UpdateArticleRequest.builder().title("Updated").build();

      assertThatThrownBy(() -> articlePopulator.update(null, dto))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Article must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when dto is null")
    void update_WhenDtoIsNull_ShouldThrowNpe() {
      Article article = Article.builder().title("Old").build();

      assertThatThrownBy(() -> articlePopulator.update(article, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("UpdateArticleRequest must not be null");
    }

    @Test
    @DisplayName("should update entity and sanitize WYSIWYG HTML content")
    void update_WithValidDto_ShouldUpdateAndSanitize() {
      UUID id = UUID.randomUUID();
      Article article =
          Article.builder().title("Old Title").slug("old-slug").build();
      article.setId(id);

      String wysiwygHtml =
          "<p>Updated <em>benchmarks</em></p><script>alert('xss')</script>";
      UpdateArticleRequest dto =
          UpdateArticleRequest.builder()
              .title("New Title")
              .slug("NEW-SLUG")
              .contentHtml(wysiwygHtml)
              .status(ArticleStatus.PUBLISHED)
              .build();

      articlePopulator.update(article, dto);

      verify(articleMapper).updateEntityFromDto(dto, article);
      assertThat(article.getSlug()).isEqualTo("new-slug");
      assertThat(article.getContentHtml()).contains("<p>Updated <em>benchmarks</em></p>");
      assertThat(article.getContentHtml()).doesNotContain("<script>");
    }
  }

  @Nested
  @DisplayName("toDto(Article article)")
  class ToDtoTests {

    @Test
    @DisplayName("should throw NullPointerException when article is null")
    void toDto_WhenArticleIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> articlePopulator.toDto(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Article must not be null");
    }

    @Test
    @DisplayName("should delegate to articleMapper and return ArticleResponse")
    void toDto_WhenArticleIsValid_ShouldReturnDto() {
      UUID id = UUID.randomUUID();
      Article article =
          Article.builder()
              .title("RTX 4090 Review")
              .slug("rtx-4090-review")
              .contentHtml("<p>Review</p>")
              .status(ArticleStatus.PUBLISHED)
              .build();
      article.setId(id);

      ArticleResponse expectedResponse =
          ArticleResponse.builder()
              .id(id)
              .title("RTX 4090 Review")
              .slug("rtx-4090-review")
              .contentHtml("<p>Review</p>")
              .status(ArticleStatus.PUBLISHED)
              .build();

      when(articleMapper.toDto(article)).thenReturn(expectedResponse);

      ArticleResponse result = articlePopulator.toDto(article);

      assertThat(result).isNotNull().isEqualTo(expectedResponse);
      verify(articleMapper).toDto(article);
    }
  }
}
