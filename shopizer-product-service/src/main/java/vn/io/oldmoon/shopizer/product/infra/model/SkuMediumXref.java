package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.common.web.model.Medium;

@Deprecated
@Entity
@Setter
@Getter
public class SkuMediumXref extends BaseEntity {
  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "sku_id")
  private Sku sku;

  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "medium_id")
  private Medium medium;
}
