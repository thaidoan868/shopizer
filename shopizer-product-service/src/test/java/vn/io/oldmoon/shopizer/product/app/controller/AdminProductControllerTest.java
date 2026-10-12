package vn.io.oldmoon.shopizer.product.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import vn.io.oldmoon.shopizer.product.app.dto.product.CreateProductRequest;
import vn.io.oldmoon.shopizer.product.app.dto.product.CreateProductResponse;
import vn.io.oldmoon.shopizer.product.app.dto.product.ProductPopulator;
import vn.io.oldmoon.shopizer.product.business.service.ProductService;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;

@WebMvcTest(controllers = AdminProductController.class)
@ContextConfiguration(
    classes = {
      AdminProductController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class AdminProductControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ProductService productService;
  @MockitoBean private ProductPopulator productPopulator;

  @Test
  @DisplayName("POST /api/v1/products with SUPER_ADMIN role should return 201 Created")
  void createProduct_WithSuperAdminRole_ShouldReturn201() throws Exception {
    UUID productId = UUID.randomUUID();
    CreateProductRequest request =
        CreateProductRequest.builder().name("RTX 3050").slug("rtx-3050").build();
    Product productEntity = Product.builder().name("RTX 3050").slug("rtx-3050").build();
    Product createdEntity = Product.builder().name("RTX 3050").slug("rtx-3050").build();
    createdEntity.setId(productId);
    CreateProductResponse response =
        CreateProductResponse.builder().id(productId).name("RTX 3050").slug("rtx-3050").build();

    given(productPopulator.toEntity(any(CreateProductRequest.class))).willReturn(productEntity);
    given(productService.create(productEntity)).willReturn(createdEntity);
    given(productPopulator.toDto(createdEntity)).willReturn(response);

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "admin")
                                    .claim("realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(productId.toString()))
        .andExpect(jsonPath("$.name").value("RTX 3050"))
        .andExpect(jsonPath("$.slug").value("rtx-3050"));
  }

  @Test
  @DisplayName("POST /api/v1/products without authentication should return 401 Unauthorized")
  void createProduct_WithoutAuth_ShouldReturn401() throws Exception {
    CreateProductRequest request = CreateProductRequest.builder().name("RTX 3050").build();

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/v1/products without SUPER_ADMIN role should return 403 Forbidden")
  void createProduct_WithoutSuperAdminRole_ShouldReturn403() throws Exception {
    CreateProductRequest request = CreateProductRequest.builder().name("RTX 3050").build();

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder
                                    .claim("preferred_username", "user")
                                    .claim("realm_access", Map.of("roles", List.of("customer"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/v1/products with invalid body should return 422 Unprocessable Entity")
  void createProduct_WithInvalidBody_ShouldReturn422() throws Exception {
    CreateProductRequest invalidRequest = CreateProductRequest.builder().name("").build();

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))))
        .andExpect(status().isUnprocessableEntity());
  }
}
