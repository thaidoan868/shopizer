package vn.io.oldmoon.shopizer.product.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.io.oldmoon.shopizer.common.core.exception.handler.ApplicationExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.product.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticlePopulator;
import vn.io.oldmoon.shopizer.product.app.dto.article.ArticleResponse;
import vn.io.oldmoon.shopizer.product.app.dto.article.CreateArticleRequest;
import vn.io.oldmoon.shopizer.product.app.dto.article.UpdateArticleRequest;
import vn.io.oldmoon.shopizer.product.business.service.ArticleService;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;
import vn.io.oldmoon.shopizer.product.infra.model.media.ArticleStatus;

@WebMvcTest(controllers = AdminArticleController.class)
@ContextConfiguration(
    classes = {
      AdminArticleController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class AdminArticleControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ArticleService articleService;
  @MockitoBean private ArticlePopulator articlePopulator;

  private static final SimpleGrantedAuthority SUPER_ADMIN_AUTH =
      new SimpleGrantedAuthority("ROLE_SUPER_ADMIN");

  @Test
  @DisplayName("POST /api/v1/articles with SUPER_ADMIN role should return 201 Created")
  void create_WithSuperAdminRole_ShouldReturn201() throws Exception {
    UUID articleId = UUID.randomUUID();
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("RTX 4090 Review")
            .slug("rtx-4090-review")
            .contentHtml("<p>Review content</p>")
            .status(ArticleStatus.PUBLISHED)
            .build();

    Article entity = Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
    Article created = Article.builder().title("RTX 4090 Review").slug("rtx-4090-review").build();
    created.setId(articleId);

    ArticleResponse response =
        ArticleResponse.builder()
            .id(articleId)
            .title("RTX 4090 Review")
            .slug("rtx-4090-review")
            .build();

    given(articlePopulator.toEntity(any(CreateArticleRequest.class))).willReturn(entity);
    given(articleService.create(entity)).willReturn(created);
    given(articlePopulator.toDto(created)).willReturn(response);

    mockMvc
        .perform(
            post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(articleId.toString()))
        .andExpect(jsonPath("$.title").value("RTX 4090 Review"))
        .andExpect(jsonPath("$.slug").value("rtx-4090-review"));
  }

  @Test
  @DisplayName("POST /api/v1/articles without authentication should return 401 Unauthorized")
  void create_WithoutAuth_ShouldReturn401() throws Exception {
    CreateArticleRequest request =
        CreateArticleRequest.builder().title("RTX 4090 Review").build();

    mockMvc
        .perform(
            post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("PUT /api/v1/articles/{id} with SUPER_ADMIN role should return 200 OK")
  void update_WithSuperAdminRole_ShouldReturn200() throws Exception {
    UUID articleId = UUID.randomUUID();
    UpdateArticleRequest request =
        UpdateArticleRequest.builder()
            .title("Updated RTX 4090 Review")
            .slug("updated-rtx-4090-review")
            .build();

    Article existing = Article.builder().title("Old Review").slug("old-review").build();
    existing.setId(articleId);

    Article updated =
        Article.builder().title("Updated RTX 4090 Review").slug("updated-rtx-4090-review").build();
    updated.setId(articleId);

    ArticleResponse response =
        ArticleResponse.builder()
            .id(articleId)
            .title("Updated RTX 4090 Review")
            .slug("updated-rtx-4090-review")
            .build();

    given(articleService.getById(articleId)).willReturn(existing);
    given(articleService.update(existing)).willReturn(updated);
    given(articlePopulator.toDto(updated)).willReturn(response);

    mockMvc
        .perform(
            put("/api/v1/articles/{id}", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(articleId.toString()))
        .andExpect(jsonPath("$.title").value("Updated RTX 4090 Review"));
  }

  @Test
  @DisplayName("PUT /api/v1/articles/{id} without authentication should return 401 Unauthorized")
  void update_WithoutAuth_ShouldReturn401() throws Exception {
    UUID articleId = UUID.randomUUID();
    UpdateArticleRequest request =
        UpdateArticleRequest.builder().title("Updated Review").build();

    mockMvc
        .perform(
            put("/api/v1/articles/{id}", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
