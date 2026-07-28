package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
public class ShopProduct extends BaseEntity {
  private String urlKey;
  private String name;
  private String description;
  private String manufacturer;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "default_sku_id")
  private Sku defaultSku;

  @Builder.Default private Boolean deleted = false;
}
