package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.common.web.model.Medium;

@Deprecated
@Slf4j
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Sku extends BaseEntity {
  private String urlKey;

  @OneToMany(
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      cascade = CascadeType.ALL,
      mappedBy = "sku")
  @Builder.Default
  @Getter(AccessLevel.PRIVATE)
  @ToString.Exclude
  private Set<SkuProductOptionValueXref> skuProductOptionValueXrefs = new HashSet<>();

  private String name;
  private String description;
  @NotNull private BigDecimal salePrice;
  @NotNull private BigDecimal cost;
  private Currency currency;
  @Builder.Default private Boolean isSalable = true;
  @Builder.Default private Boolean discontinued = false;
  @Builder.Default private Set<Tax> taxes = new HashSet<>();
  @Builder.Default private Boolean vatable = true;

  @OneToMany(
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      mappedBy = "sku",
      cascade = CascadeType.ALL)
  @Builder.Default
  @ToString.Exclude
  @Getter(AccessLevel.PRIVATE)
  private Set<SkuMediumXref> skuMediumXrefs = new HashSet<>();

  @OneToMany(
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY,
      mappedBy = "sku")
  private List<SkuAttribute> attributes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  @ToString.Exclude
  private Product product;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "defaultSku")
  @Builder.Default
  @ToString.Exclude
  private List<Product> defaultSkuOfProducts = new ArrayList<>();

  private Integer availableQuantity;
  @Builder.Default private Integer incrementStep = 1;
  @Builder.Default private Integer minOrderQuantity = 1;
  @Builder.Default private Boolean deleted = false;

  public Set<ProductOptionValue> getProductOptionValues() {
    Set<ProductOptionValue> values = new HashSet<>();
    skuProductOptionValueXrefs.forEach(
        xref -> {
          values.add(xref.getProductOptionValue());
        });
    return Set.copyOf(values);
  }

  public void addProductOptionValue(ProductOptionValue newValue) {
    if (newValue == null) {
      log.warn("Product option value is empty");
      return;
    }
    if (getProductOptionValues().contains(newValue)) {
      log.warn("Product option value already exists");
    } else {
      SkuProductOptionValueXref newXref = new SkuProductOptionValueXref();
      newXref.setProductOptionValue(newValue);
      newXref.setSku(this);
      skuProductOptionValueXrefs.add(newXref);
    }
  }

  public void removeProductOptionValue(ProductOptionValue value) {

    boolean removed =
        skuProductOptionValueXrefs.removeIf(xref -> xref.getProductOptionValue().equals(value));
    if (!removed) {
      log.warn("No product option value found with id {}", value.getId());
    }
  }

  public Set<Medium> getMedia() {
    Set<Medium> media = new HashSet<>();
    skuMediumXrefs.forEach(
        xref -> {
          media.add(xref.getMedium());
        });
    return Set.copyOf(media);
  }

  public void addMedium(Medium medium) {
    if (medium == null) {
      log.warn("Medium value is empty");
      return;
    }
    if (getMedia().contains(medium)) {
      log.warn("Medium value already exists");
    } else {
      SkuMediumXref newXref = new SkuMediumXref();
      newXref.setMedium(medium);
      newXref.setSku(this);
      skuMediumXrefs.add(newXref);
    }
  }

  public void removeMedium(Medium medium) {
    boolean removed = skuMediumXrefs.removeIf(xref -> xref.getMedium().equals(medium));
    if (!removed) {
      log.warn("No medium  found with id {}", medium.getId());
    }
  }
}
