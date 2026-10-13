package vn.io.oldmoon.shopizer.product.app.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.handler.ApplicationExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.product.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticlePopulator;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticleResponse;
import vn.io.oldmoon.shopizer.product.business.service.ArticleService;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

@WebMvcTest(controllers = PublicArticleController.class)
@ContextConfiguration(
    classes = {
      PublicArticleController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class PublicArticleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ArticleService articleService;
  @MockitoBean private ArticlePopulator articlePopulator;

  @Test
  @DisplayName("GET /api/v1/public/articles/{idOrSlug} with UUID should return 200 OK without authentication")
  void getByIdOrSlug_WithUuid_ShouldReturn200() throws Exception {
    UUID articleId = UUID.randomUUID();
    String idOrSlug = articleId.toString();
    Article article = Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
    article.setId(articleId);

    ArticleResponse response =
        ArticleResponse.builder()
            .id(articleId)
            .title("RTX 4090 Review")
            .slug("rtx-4090-review")
            .status(ArticleStatus.PUBLISHED)
            .created(Instant.now())
            .build();

    given(articleService.getByIdOrSlug(idOrSlug)).willReturn(article);
    given(articlePopulator.toDto(article)).willReturn(response);

    mockMvc
        .perform(get("/api/v1/public/articles/{idOrSlug}", idOrSlug))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(articleId.toString()))
        .andExpect(jsonPath("$.title").value("RTX 4090 Review"))
        .andExpect(jsonPath("$.slug").value("rtx-4090-review"));
  }

  @Test
  @DisplayName("GET /api/v1/public/articles/{idOrSlug} with slug should return 200 OK without authentication")
  void getByIdOrSlug_WithSlug_ShouldReturn200() throws Exception {
    String slug = "rtx-4090-review";
    UUID articleId = UUID.randomUUID();
    Article article = Article.builder().title("RTX 4090 Review").slug(slug).build();
    article.setId(articleId);

    ArticleResponse response =
        ArticleResponse.builder()
            .id(articleId)
            .title("RTX 4090 Review")
            .slug(slug)
            .status(ArticleStatus.PUBLISHED)
            .created(Instant.now())
            .build();

    given(articleService.getByIdOrSlug(slug)).willReturn(article);
    given(articlePopulator.toDto(article)).willReturn(response);

    mockMvc
        .perform(get("/api/v1/public/articles/{idOrSlug}", slug))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(articleId.toString()))
        .andExpect(jsonPath("$.title").value("RTX 4090 Review"))
        .andExpect(jsonPath("$.slug").value(slug));
  }

  @Test
  @DisplayName("GET /api/v1/public/articles/{idOrSlug} when not found should return 404 NOT_FOUND")
  void getByIdOrSlug_NotFound_ShouldReturn404() throws Exception {
    String slug = "non-existent-article";
    given(articleService.getByIdOrSlug(slug))
        .willThrow(new ResourceNotFoundException("Article", slug));

    mockMvc
        .perform(get("/api/v1/public/articles/{idOrSlug}", slug))
        .andExpect(status().isNotFound());
  }
}
