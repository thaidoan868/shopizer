package vn.io.oldmoon.shopizer.product.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.AttributeTemplatePopulator;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.AttributeTemplateResponse;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.CreateAttributeTemplateRequest;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.ListAttributeTemplateResponse;
import vn.io.oldmoon.shopizer.product.app.dto.attributetemplate.UpdateAttributeTemplateRequest;
import vn.io.oldmoon.shopizer.product.business.service.AttributeTemplateService;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/attribute-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Attribute Template Endpoints",
    description = "Endpoints for managing attribute templates")
public class AdminAttributeTemplateController extends AbstractController {

  private final AttributeTemplateService attributeTemplateService;
  private final AttributeTemplatePopulator attributeTemplatePopulator;

  @PostMapping
  @Operation(
      summary = "Create attribute template",
      description = "Creates a new attribute template")
  @ApiResponse(
      responseCode = "201",
      description = "Attribute template created successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = AttributeTemplateResponse.class)))
  public ResponseEntity<AttributeTemplateResponse> create(
      @Valid @RequestBody CreateAttributeTemplateRequest createDto) {
    AttributeTemplate entity = attributeTemplatePopulator.toEntity(createDto);
    AttributeTemplate created = attributeTemplateService.create(entity);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(attributeTemplatePopulator.toDto(created));
  }

  @GetMapping
  @Operation(
      summary = "Get all attribute templates",
      description = "Retrieves a list of all attribute templates")
  @ApiResponse(
      responseCode = "200",
      description = "Attribute templates retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ListAttributeTemplateResponse.class)))
  public ResponseEntity<ListAttributeTemplateResponse> getAll() {
    List<AttributeTemplate> templates = attributeTemplateService.getAll();
    return ResponseEntity.ok(attributeTemplatePopulator.toListResponse(templates));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get attribute template by ID",
      description = "Retrieves a specific attribute template by its UUID")
  @ApiResponse(
      responseCode = "200",
      description = "Attribute template retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = AttributeTemplateResponse.class)))
  public ResponseEntity<AttributeTemplateResponse> getById(@PathVariable("id") UUID id) {
    AttributeTemplate template = attributeTemplateService.getById(id);
    return ResponseEntity.ok(attributeTemplatePopulator.toDto(template));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update attribute template",
      description = "Updates an existing attribute template by its UUID")
  @ApiResponse(
      responseCode = "200",
      description = "Attribute template updated successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = AttributeTemplateResponse.class)))
  public ResponseEntity<AttributeTemplateResponse> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateAttributeTemplateRequest updateDto) {
    AttributeTemplate existing = attributeTemplateService.getById(id);
    attributeTemplatePopulator.update(existing, updateDto);
    AttributeTemplate updated = attributeTemplateService.update(existing);
    return ResponseEntity.ok(attributeTemplatePopulator.toDto(updated));
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete attribute template",
      description = "Deletes an attribute template by its UUID")
  @ApiResponse(responseCode = "204", description = "Attribute template deleted successfully")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    attributeTemplateService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
