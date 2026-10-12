package vn.io.oldmoon.shopizer.product.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.product.app.dto.product.CreateProductRequest;
import vn.io.oldmoon.shopizer.product.app.dto.product.CreateProductResponse;
import vn.io.oldmoon.shopizer.product.app.dto.product.ProductPopulator;
import vn.io.oldmoon.shopizer.product.business.service.ProductService;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Endpoints", description = "Endpoints for managing products")
public class AdminProductController extends AbstractController {

  private final ProductService productService;
  private final ProductPopulator productPopulator;

  @PostMapping
  @Operation(
      summary = "Create product",
      description = "Creates a new product with the provided details")
  @ApiResponse(
      responseCode = "201",
      description = "Product created successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CreateProductResponse.class)))
  public ResponseEntity<CreateProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest createProductRequest) {
    Product product = productPopulator.toEntity(createProductRequest);
    Product createdProduct = productService.create(product);
    CreateProductResponse responseDto = productPopulator.toDto(createdProduct);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }
  //
  //  @GetMapping("/{identifier}")
  //  @Operation(
  //      summary = "Get product by identifier",
  //      description = "Retrieves a product using either its UUID or its slug")
  //  @ApiResponse(
  //      responseCode = "200",
  //      description = "Product retrieved successfully",
  //      content =
  //          @Content(
  //              mediaType = MediaType.APPLICATION_JSON_VALUE,
  //              schema = @Schema(implementation = CreateProductResponse.class)))
  //  public ResponseEntity<CreateProductResponse> getProductByIdentifier(@PathVariable String
  // identifier) {
  //    Product product = productService.getByIdentifier(identifier);
  //    CreateProductResponse productDto = productPopulator.toDto(product);
  //    return ResponseEntity.ok(productDto);
  //  }
}
