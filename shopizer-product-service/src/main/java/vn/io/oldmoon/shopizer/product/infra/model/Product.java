package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Deprecated
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Slf4j
public class Product extends BaseEntity {
  @Builder.Default
  @ToString.Exclude
  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private HashSet<ProductAttribute> productAttributes = new HashSet<>();

  @Builder.Default
  @ToString.Exclude
  @Getter(AccessLevel.PROTECTED)
  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private HashSet<ProductOptionXref> productOptionXrefs = new HashSet<>();

  @Builder.Default
  @ToString.Exclude
  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = false)
  private HashSet<Sku> skus = new HashSet<>();

  @Builder.Default
  @ToString.Exclude
  @Getter(AccessLevel.PROTECTED)
  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private HashSet<RelatedProductXref> relatedTo = new HashSet<>();

  @Getter(AccessLevel.PROTECTED)
  @Builder.Default
  @ToString.Exclude
  @OneToMany(
      mappedBy = "relatedProduct",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private HashSet<RelatedProductXref> relatedFrom = new HashSet<>();

  private String urlKey;
  private String name;
  private String description;
  private String manufacturer;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "default_sku_id")
  private Sku defaultSku;

  @Builder.Default private Boolean deleted = false;

  public Set<ProductOption> getProductOptions() {
    HashSet<ProductOption> productOptions = new HashSet<>();
    productOptionXrefs.forEach(xref -> productOptions.add(xref.getProductOption()));
    return Set.copyOf(productOptions);
  }

  public void addProductOption(ProductOption newOption) {
    if (newOption == null) {
      log.warn("Product option is null");
      return;
    }

    if (getProductOptions().contains(newOption)) {
      log.warn("Product option already exists");
    } else {
      ProductOptionXref newXref = new ProductOptionXref();
      newXref.setProductOption(newOption);
      newXref.setProduct(this);
      productOptionXrefs.add(newXref);
    }
  }

  public void removeProductOption(ProductOption option) {
    boolean removed = productOptionXrefs.removeIf(xref -> xref.getProductOption().equals(option));
    if (!removed) {
      log.warn("No product option found with id {}", option.getId());
    }
  }

  public Set<Product> getRelatedProducts() {
    HashSet<Product> relatedProducts = new HashSet<>();
    // Get the targets of my outgoing relations
    relatedTo.forEach(xref -> relatedProducts.add(xref.getRelatedProduct()));
    // Get the sources of my incoming relations
    relatedFrom.forEach(xref -> relatedProducts.add(xref.getProduct()));
    return Set.copyOf(relatedProducts);
  }

  /** Warning: the related products of other won't be updated after the adding process */
  public void addRelatedProduct(Product other) {
    if (other == null || other.equals(this)) {
      log.warn("Product is null or the product is the same as the current product");
      return;
    }

    if (other.getId() == null) {
      log.warn("The product id is null. Can't add an unsaved product");
    }

    if (getRelatedProducts().contains(other)) {
      log.warn("Trying to add an existing relationship, relatedProductId={}", other);
    } else {
      RelatedProductXref xref = new RelatedProductXref();
      xref.setProduct(this);
      xref.setRelatedProduct(other);
      relatedTo.add(xref);
    }
  }

  /** Warning: the related products of other won't be updated after the removing process */
  public void removeRelatedProduct(Product other) {
    if (other == null) {
      return;
    }
    boolean removedFromTo = relatedTo.removeIf(xref -> xref.getRelatedProduct().equals(other));
    boolean removedFromFrom = relatedFrom.removeIf(xref -> xref.getProduct().equals(other));
    if (!removedFromTo && !removedFromFrom) {
      log.warn("Trying to remove a non-existing relationship, relatedProductId={}", other);
    }
  }
}
