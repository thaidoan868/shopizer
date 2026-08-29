package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Slf4j
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class ShopSku extends BaseEntity {
  private String urlKey;
  private String name;
  private String description;
  @NotNull private BigDecimal salePrice;
  @NotNull private BigDecimal cost;
  private Currency currency;
  @Builder.Default private Boolean isSalable = true;
  @Builder.Default private Boolean discontinued = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  @ToString.Exclude
  private ShopProduct product;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "defaultSku")
  @Builder.Default
  @ToString.Exclude
  private List<ShopProduct> defaultSkuOfProducts = new ArrayList<>();

  private Integer availableQuantity;
  @Builder.Default private Boolean deleted = false;
}