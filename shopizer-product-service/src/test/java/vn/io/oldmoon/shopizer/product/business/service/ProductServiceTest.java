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
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;
import vn.io.oldmoon.shopizer.product.infra.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductService productService;

  @Nested
  @DisplayName("create(Product product)")
  class CreateTests {

    @Test
    @DisplayName("should throw NullPointerException when product is null")
    void create_WhenProductIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> productService.create(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Product must not be null");
    }

    @Test
    @DisplayName("should save and return product when product is valid")
    void create_WhenProductIsValid_ShouldSaveAndReturnProduct() {
      // Given
      Product product =
          Product.builder().name("Test Product").slug("test-product").build();
      when(productRepository.save(product)).thenReturn(product);

      // When
      Product result = productService.create(product);

      // Then
      assertThat(result).isNotNull().isEqualTo(product);
      verify(productRepository).save(product);
    }
  }

  @Nested
  @DisplayName("getByIdentifier(String identifier)")
  class GetByIdentifierTests {

    @Test
    @DisplayName("should throw InvalidInputException when identifier is null")
    void getByIdentifier_WhenIdentifierIsNull_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> productService.getByIdentifier(null))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Product identifier must not be blank");
    }

    @Test
    @DisplayName("should throw InvalidInputException when identifier is empty")
    void getByIdentifier_WhenIdentifierIsEmpty_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> productService.getByIdentifier(""))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Product identifier must not be blank");
    }

    @Test
    @DisplayName("should throw InvalidInputException when identifier is blank")
    void getByIdentifier_WhenIdentifierIsBlank_ShouldThrowInvalidInputException() {
      assertThatThrownBy(() -> productService.getByIdentifier("   "))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Product identifier must not be blank");
    }

    @Test
    @DisplayName("should return product when found by ID")
    void getByIdentifier_WhenFoundById_ShouldReturnProduct() {
      // Given
      UUID id = UUID.randomUUID();
      Product product = Product.builder().name("Product 1").slug("product-1").build();
      product.setId(id);
      when(productRepository.findById(id)).thenReturn(Optional.of(product));

      // When
      Product result = productService.getByIdentifier(id.toString());

      // Then
      assertThat(result).isNotNull().isEqualTo(product);
      verify(productRepository).findById(id);
    }

    @Test
    @DisplayName("should return product when not found by ID but found by exact slug")
    void getByIdentifier_WhenNotFoundById_AndFoundBySlug_ShouldReturnProduct() {
      // Given
      UUID id = UUID.randomUUID();
      String identifier = id.toString();
      Product product = Product.builder().name("Product 1").slug(identifier).build();
      when(productRepository.findById(id)).thenReturn(Optional.empty());
      when(productRepository.findBySlug(identifier)).thenReturn(Optional.of(product));

      // When
      Product result = productService.getByIdentifier(identifier);

      // Then
      assertThat(result).isNotNull().isEqualTo(product);
      verify(productRepository).findById(id);
      verify(productRepository).findBySlug(identifier);
    }

    @Test
    @DisplayName("should return product when not found by ID or slug, but found by slug ignore case")
    void getByIdentifier_WhenFoundBySlugIgnoreCase_ShouldReturnProduct() {
      // Given
      UUID id = UUID.randomUUID();
      String identifier = id.toString();
      Product product = Product.builder().name("Product 1").slug(identifier).build();
      when(productRepository.findById(id)).thenReturn(Optional.empty());
      when(productRepository.findBySlug(identifier)).thenReturn(Optional.empty());
      when(productRepository.findBySlugIgnoreCase(identifier)).thenReturn(Optional.of(product));

      // When
      Product result = productService.getByIdentifier(identifier);

      // Then
      assertThat(result).isNotNull().isEqualTo(product);
      verify(productRepository).findById(id);
      verify(productRepository).findBySlug(identifier);
      verify(productRepository).findBySlugIgnoreCase(identifier);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when product is not found by ID or slug")
    void getByIdentifier_WhenNotFound_ShouldThrowResourceNotFoundException() {
      // Given
      UUID id = UUID.randomUUID();
      String identifier = id.toString();
      when(productRepository.findById(id)).thenReturn(Optional.empty());
      when(productRepository.findBySlug(identifier)).thenReturn(Optional.empty());
      when(productRepository.findBySlugIgnoreCase(identifier)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> productService.getByIdentifier(identifier))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Product")
          .hasMessageContaining(identifier);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when identifier is not a valid UUID format")
    void getByIdentifier_WhenIdentifierIsNotValidUuid_ShouldThrowIllegalArgumentException() {
      assertThatThrownBy(() -> productService.getByIdentifier("not-a-valid-uuid"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
