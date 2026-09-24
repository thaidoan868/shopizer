package vn.io.oldmoon.shopizer.product.infra.model.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "skus")
public class Sku extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Product product;

  @Column(name = "sku_code", unique = true, length = 100)
  private String skuCode;

  @Column(unique = true, length = 100, nullable = false)
  @NotBlank
  private String barcode;

  @Column(nullable = false, precision = 15, scale = 2)
  @NotNull
  private BigDecimal price;

  @Column(nullable = false, precision = 15, scale = 2)
  @NotNull
  private BigDecimal cost;

  @Column(nullable = false, length = 25)
  @NotNull
  @Builder.Default
  private ProductStatus status = ProductStatus.ACTIVE;

  @Builder.Default
  @Column(nullable = false)
  @NotNull
  private Boolean deleted = false;
}
