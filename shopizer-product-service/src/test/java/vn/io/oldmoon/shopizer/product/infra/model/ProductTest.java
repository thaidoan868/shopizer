package vn.io.oldmoon.shopizer.product.infra.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProductTest {

  private Product product;

  @BeforeEach
  void setUp() {
    product = Product.builder().name("Test Product").urlKey("test-product").build();
  }

  @Nested
  class InitializationTests {
    @Test
    void shouldInitializeCollectionsWithDefaults() {
      Product newProduct = new Product();
      assertThat(newProduct.getProductAttributes()).isNotNull().isEmpty();
      assertThat(newProduct.getSkus()).isNotNull().isEmpty();
      assertThat(newProduct.getDeleted()).isFalse();
    }
  }

  @Nested
  class ProductOptionTests {
    private ProductOption option;

    @BeforeEach
    void setUp() {
      option = new ProductOption();
      option.setId(UUID.randomUUID());
      option.setName("Size");
    }

    @Test
    void addProductOption_ShouldAddXref_WhenOptionIsNew() {
      product.addProductOption(option);

      Set<ProductOption> options = product.getProductOptions();
      assertThat(options).containsExactly(option);
    }

    @Test
    void addProductOption_ShouldNotAddDuplicate_WhenOptionAlreadyExists() {
      // Add twice
      product.addProductOption(option);
      product.addProductOption(option);

      assertThat(product.getProductOptions()).hasSize(1);
    }

    @Test
    void addProductOption_ShouldLogWarningAndGracefullyIgnore_WhenOptionIsNull() {
      product.addProductOption(null);
      assertThat(product.getProductOptions()).isEmpty();
    }

    @Test
    void removeProductOption_ShouldRemoveXref_WhenOptionExists() {
      product.addProductOption(option);

      product.removeProductOption(option);

      assertThat(product.getProductOptions()).isEmpty();
    }

    @Test
    void removeProductOption_ShouldDoNothing_WhenOptionDoesNotExist() {
      product.removeProductOption(option);

      assertThat(product.getProductOptions()).isEmpty();
    }
  }

  @Nested
  class RelatedProductTests {

    private Product otherProduct;

    @BeforeEach
    void setUp() {
      UUID currentProductId = UUID.randomUUID();
      UUID otherProductId = UUID.randomUUID();

      // Assigning IDs since the code explicitly logs/checks for it
      product.setId(currentProductId);

      otherProduct = Product.builder().name("Other Product").urlKey("other-product").build();
      otherProduct.setId(otherProductId);
    }

    @Test
    void getRelatedProducts_ShouldCombine_RelatedToAndRelatedFrom() {
      // Setup an outgoing relationship (RelatedTo)
      product.addRelatedProduct(otherProduct);
      // Setup an incoming relationship (RelatedFrom)
      Product incomingProduct = Product.builder().name("Incoming").build();
      incomingProduct.setId(UUID.randomUUID());

      RelatedProductXref incomingXref = new RelatedProductXref();
      incomingXref.setProduct(incomingProduct);
      incomingXref.setRelatedProduct(product);
      product.getRelatedFrom().add(incomingXref);

      // Action
      Set<Product> relatedProducts = product.getRelatedProducts();

      // Assert
      assertThat(relatedProducts).containsExactlyInAnyOrder(otherProduct, incomingProduct);
    }

    @Test
    void addRelatedProduct_ShouldAddXrefToRelatedTo_WhenProductIsValidAndNew() {
      product.addRelatedProduct(otherProduct);

      assertThat(product.getRelatedProducts()).containsExactly(otherProduct);
    }

    @Test
    void addRelatedProduct_ShouldNotAddRelation_WhenOtherProductIsSameAsCurrent() {
      product.addRelatedProduct(product);

      assertThat(product.getRelatedProducts()).isEmpty();
    }

    @Test
    void addRelatedProduct_ShouldNotAddRelation_WhenOtherProductIsNull() {
      product.addRelatedProduct(null);

      assertThat(product.getRelatedProducts()).isEmpty();
    }

    @Test
    void addRelatedProduct_ShouldNotAddDuplicate_WhenRelationAlreadyExists() {
      product.addRelatedProduct(otherProduct);
      product.addRelatedProduct(otherProduct);

      assertThat(product.getRelatedProducts()).hasSize(1);
    }

    @Test
    void
        remoeRelatedProduct_ShouldRemoveRelatedFromXref_WhenRelatedProductIsRelatedFromRelationship() {}

    @Test
    void remoeRelatedProduct_ShouldRemoveRelatedToXref_WhenRelatedProductIsRelatedToRelationship() {
      // Setup cross links in lists
      RelatedProductXref toXref = new RelatedProductXref();
      toXref.setProduct(product);
      toXref.setRelatedProduct(otherProduct);
      product.getRelatedTo().add(toXref);

      RelatedProductXref fromXref = new RelatedProductXref();
      fromXref.setProduct(otherProduct);
      fromXref.setRelatedProduct(product);
      product.getRelatedFrom().add(fromXref);

      assertThat(product.getRelatedProducts()).hasSize(1);

      // Action
      product.removeRelatedProduct(otherProduct);

      // Assert
      assertThat(product.getRelatedProducts()).isEmpty();
    }

    @Test
    void removeRelatedProduct_ShouldDoNothing_WhenOtherProductIsNull() {
      product.removeRelatedProduct(null);
      // Verify no exceptions are thrown
    }
  }
}
