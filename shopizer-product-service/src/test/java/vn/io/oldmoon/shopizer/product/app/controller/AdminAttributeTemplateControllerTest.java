package vn.io.oldmoon.shopizer.product.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.handler.ApplicationExceptionHandler;
import vn.io.oldmoon.shopizer.common.core.exception.handler.GlobalExceptionHandler;
import vn.io.oldmoon.shopizer.product.app.config.SecurityConfig;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.AttributeTemplatePopulator;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.AttributeTemplateResponse;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.CreateAttributeTemplateRequest;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.ListAttributeTemplateResponse;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.ShortAttributeTemplateResponse;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.UpdateAttributeTemplateRequest;
import vn.io.oldmoon.shopizer.product.business.service.AttributeTemplateService;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

@WebMvcTest(controllers = AdminAttributeTemplateController.class)
@ContextConfiguration(
    classes = {
      AdminAttributeTemplateController.class,
      SecurityConfig.class,
      ApplicationExceptionHandler.class,
      GlobalExceptionHandler.class
    })
class AdminAttributeTemplateControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AttributeTemplateService attributeTemplateService;
  @MockitoBean private AttributeTemplatePopulator attributeTemplatePopulator;

  private static final SimpleGrantedAuthority SUPER_ADMIN_AUTH =
      new SimpleGrantedAuthority("ROLE_SUPER_ADMIN");

  @Test
  @DisplayName("POST /api/v1/attribute-templates with valid data should return 201 Created")
  void create_WithSuperAdminRole_ShouldReturn201() throws Exception {
    UUID id = UUID.randomUUID();
    CreateAttributeTemplateRequest request =
        CreateAttributeTemplateRequest.builder().name("GPU Template").code("GPU_TMPL").build();
    AttributeTemplate entity =
        AttributeTemplate.builder().name("GPU Template").code("GPU_TMPL").build();
    AttributeTemplate createdEntity =
        AttributeTemplate.builder().name("GPU Template").code("GPU_TMPL").build();
    createdEntity.setId(id);
    AttributeTemplateResponse response =
        AttributeTemplateResponse.builder().id(id).name("GPU Template").code("GPU_TMPL").build();

    given(attributeTemplatePopulator.toEntity(any(CreateAttributeTemplateRequest.class)))
        .willReturn(entity);
    given(attributeTemplateService.create(entity)).willReturn(createdEntity);
    given(attributeTemplatePopulator.toDto(createdEntity)).willReturn(response);

    mockMvc
        .perform(
            post("/api/v1/attribute-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("GPU Template"))
        .andExpect(jsonPath("$.code").value("GPU_TMPL"));
  }

  @Test
  @DisplayName("POST /api/v1/attribute-templates without authentication should return 401 Unauthorized")
  void create_WithoutAuth_ShouldReturn401() throws Exception {
    CreateAttributeTemplateRequest request =
        CreateAttributeTemplateRequest.builder().name("GPU Template").code("GPU_TMPL").build();

    mockMvc
        .perform(
            post("/api/v1/attribute-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/v1/attribute-templates with non-admin role should return 403 Forbidden")
  void create_WithCustomerRole_ShouldReturn403() throws Exception {
    CreateAttributeTemplateRequest request =
        CreateAttributeTemplateRequest.builder().name("GPU Template").code("GPU_TMPL").build();

    mockMvc
        .perform(
            post("/api/v1/attribute-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("customer"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/v1/attribute-templates with invalid body should return 422 Unprocessable Entity")
  void create_WithInvalidBody_ShouldReturn422() throws Exception {
    CreateAttributeTemplateRequest invalidRequest =
        CreateAttributeTemplateRequest.builder().name("").code("").build();

    mockMvc
        .perform(
            post("/api/v1/attribute-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("GET /api/v1/attribute-templates should return 200 OK with list of templates")
  void getAll_ShouldReturn200() throws Exception {
    AttributeTemplate t = AttributeTemplate.builder().name("T").code("C").build();
    List<AttributeTemplate> list = List.of(t);
    UUID templateId = UUID.randomUUID();
    ShortAttributeTemplateResponse shortDto =
        ShortAttributeTemplateResponse.builder().id(templateId).name("T").code("C").build();
    ListAttributeTemplateResponse listResponse =
        ListAttributeTemplateResponse.builder()
            .templates(List.of(shortDto))
            .totalCount(1)
            .build();

    given(attributeTemplateService.getAll()).willReturn(list);
    given(attributeTemplatePopulator.toListResponse(list)).willReturn(listResponse);

    mockMvc
        .perform(
            get("/api/v1/attribute-templates")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.templates[0].name").value("T"))
        .andExpect(jsonPath("$.templates[0].code").value("C"));
  }

  @Test
  @DisplayName("GET /api/v1/attribute-templates/{id} should return 200 OK when found")
  void getById_WhenFound_ShouldReturn200() throws Exception {
    UUID id = UUID.randomUUID();
    AttributeTemplate entity = AttributeTemplate.builder().name("T").code("C").build();
    entity.setId(id);
    AttributeTemplateResponse response =
        AttributeTemplateResponse.builder().id(id).name("T").code("C").build();

    given(attributeTemplateService.getById(id)).willReturn(entity);
    given(attributeTemplatePopulator.toDto(entity)).willReturn(response);

    mockMvc
        .perform(
            get("/api/v1/attribute-templates/{id}", id)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("T"))
        .andExpect(jsonPath("$.code").value("C"));
  }

  @Test
  @DisplayName("GET /api/v1/attribute-templates/{id} should return 404 Not Found when missing")
  void getById_WhenNotFound_ShouldReturn404() throws Exception {
    UUID id = UUID.randomUUID();
    given(attributeTemplateService.getById(id))
        .willThrow(new ResourceNotFoundException("AttributeTemplate", id.toString()));

    mockMvc
        .perform(
            get("/api/v1/attribute-templates/{id}", id)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Resource not found"));
  }

  @Test
  @DisplayName("GET /api/v1/attribute-templates/{id} with invalid UUID should return 400 Bad Request")
  void getById_WhenInvalidUuid_ShouldReturn400() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/attribute-templates/{id}", "not-and-uuid")
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad request"));
  }

  @Test
  @DisplayName("PUT /api/v1/attribute-templates/{id} should return 200 OK when updated")
  void update_WhenValid_ShouldReturn200() throws Exception {
    UUID id = UUID.randomUUID();
    UpdateAttributeTemplateRequest request =
        UpdateAttributeTemplateRequest.builder().name("Updated").code("UPDATED").build();
    AttributeTemplate existing = AttributeTemplate.builder().name("Old").code("OLD").build();
    existing.setId(id);
    AttributeTemplate updated = AttributeTemplate.builder().name("Updated").code("UPDATED").build();
    updated.setId(id);
    AttributeTemplateResponse response =
        AttributeTemplateResponse.builder().id(id).name("Updated").code("UPDATED").build();

    given(attributeTemplateService.getById(id)).willReturn(existing);
    doNothing().when(attributeTemplatePopulator).update(existing, request);
    given(attributeTemplateService.update(existing)).willReturn(updated);
    given(attributeTemplatePopulator.toDto(updated)).willReturn(response);

    mockMvc
        .perform(
            put("/api/v1/attribute-templates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Updated"))
        .andExpect(jsonPath("$.code").value("UPDATED"));
  }

  @Test
  @DisplayName("PUT /api/v1/attribute-templates/{id} should return 404 when not found")
  void update_WhenNotFound_ShouldReturn404() throws Exception {
    UUID id = UUID.randomUUID();
    UpdateAttributeTemplateRequest request =
        UpdateAttributeTemplateRequest.builder().name("Updated").code("UPDATED").build();

    given(attributeTemplateService.getById(id))
        .willThrow(new ResourceNotFoundException("AttributeTemplate", id.toString()));

    mockMvc
        .perform(
            put("/api/v1/attribute-templates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Resource not found"));
  }

  @Test
  @DisplayName("DELETE /api/v1/attribute-templates/{id} should return 204 No Content")
  void delete_WhenValid_ShouldReturn204() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(attributeTemplateService).delete(id);

    mockMvc
        .perform(
            delete("/api/v1/attribute-templates/{id}", id)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/v1/attribute-templates/{id} should return 404 when not found")
  void delete_WhenNotFound_ShouldReturn404() throws Exception {
    UUID id = UUID.randomUUID();
    doThrow(new ResourceNotFoundException("AttributeTemplate", id.toString()))
        .when(attributeTemplateService)
        .delete(id);

    mockMvc
        .perform(
            delete("/api/v1/attribute-templates/{id}", id)
                .with(
                    jwt()
                        .jwt(
                            jwtBuilder ->
                                jwtBuilder.claim(
                                    "realm_access", Map.of("roles", List.of("super_admin"))))
                        .authorities(SUPER_ADMIN_AUTH)))
        .andExpect(status().isNotFound());
  }
}
