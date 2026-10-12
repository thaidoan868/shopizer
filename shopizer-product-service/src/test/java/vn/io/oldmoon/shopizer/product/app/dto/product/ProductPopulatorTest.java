package vn.io.oldmoon.shopizer.product.app.dto.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;
import vn.io.oldmoon.shopizer.product.infra.model.product.ProductStatus;

@ExtendWith(MockitoExtension.class)
class ProductPopulatorTest {

  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductPopulator productPopulator;

  @Nested
  @DisplayName("toEntity(CreateProductRequest dto)")
  class ToEntityTests {

    @Test
    @DisplayName("should throw NullPointerException when dto is null")
    void toEntity_WhenDtoIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> productPopulator.toEntity(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should normalize explicit slug when provided")
    void toEntity_WhenSlugProvided_ShouldNormalizeSlug() {
      CreateProductRequest request =
          CreateProductRequest.builder()
              .name("RTX 4090")
              .slug("  RTX-4090-SPECIAL  ")
              .status(ProductStatus.DISCONTINUED)
              .serialized(true)
              .build();

      Product product =
          Product.builder()
              .name("RTX 4090")
              .slug("  RTX-4090-SPECIAL  ")
              .status(ProductStatus.DISCONTINUED)
              .serialized(true)
              .build();

      when(productMapper.toEntity(request)).thenReturn(product);

      Product result = productPopulator.toEntity(request);

      assertThat(result).isNotNull();
      assertThat(result.getSlug()).isEqualTo("rtx-4090-special");
      assertThat(result.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
      assertThat(result.getSerialized()).isTrue();
    }

    @Test
    @DisplayName("should auto-generate slug and default status and serialized when missing")
    void toEntity_WhenSlugMissing_ShouldAutoGenerateSlugAndSetDefaults() {
      CreateProductRequest request =
          CreateProductRequest.builder().name("  NVIDIA GeForce RTX 3050  ").build();

      Product product =
          Product.builder().name("  NVIDIA GeForce RTX 3050  ").status(null).serialized(null).build();

      when(productMapper.toEntity(request)).thenReturn(product);

      Product result = productPopulator.toEntity(request);

      assertThat(result).isNotNull();
      assertThat(result.getSlug()).startsWith("nvidia-geforce-rtx-3050-");
      assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
      assertThat(result.getSerialized()).isFalse();
    }

    @Test
    @DisplayName("should not generate slug when both slug and name are null or blank")
    void toEntity_WhenSlugAndNameAreBlank_ShouldNotGenerateSlug() {
      CreateProductRequest request = CreateProductRequest.builder().name("   ").slug("").build();

      Product product = Product.builder().name("   ").slug("").build();

      when(productMapper.toEntity(request)).thenReturn(product);

      Product result = productPopulator.toEntity(request);

      assertThat(result).isNotNull();
      assertThat(result.getSlug()).isEmpty();
      assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
      assertThat(result.getSerialized()).isFalse();
    }
  }

  @Nested
  @DisplayName("toDto(Product product)")
  class ToDtoTests {

    @Test
    @DisplayName("should throw NullPointerException when product is null")
    void toDto_WhenProductIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> productPopulator.toDto(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should map Product to CreateProductResponse")
    void toDto_WhenProductIsValid_ShouldReturnResponse() {
      UUID id = UUID.randomUUID();
      Product product = Product.builder().name("Product 1").build();
      product.setId(id);

      CreateProductResponse expected = CreateProductResponse.builder().id(id).name("Product 1").build();
      when(productMapper.toDto(product)).thenReturn(expected);

      CreateProductResponse result = productPopulator.toDto(product);

      assertThat(result).isNotNull().isEqualTo(expected);
      verify(productMapper).toDto(product);
    }
  }

  @Nested
  @DisplayName("toDtoList(List<Product> products)")
  class ToDtoListTests {

    @Test
    @DisplayName("should throw NullPointerException when products is null")
    void toDtoList_WhenProductsIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> productPopulator.toDtoList(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should map list of Product entities to list of CreateProductResponse")
    void toDtoList_WhenProductsIsValid_ShouldReturnResponseList() {
      Product product = Product.builder().name("Product 1").build();
      List<Product> products = List.of(product);
      CreateProductResponse response = CreateProductResponse.builder().name("Product 1").build();
      List<CreateProductResponse> expected = List.of(response);

      when(productMapper.toDtoList(products)).thenReturn(expected);

      List<CreateProductResponse> result = productPopulator.toDtoList(products);

      assertThat(result).isNotNull().hasSize(1).isEqualTo(expected);
      verify(productMapper).toDtoList(products);
    }
  }
}
