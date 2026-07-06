package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Deprecated
@Entity
@Getter
@Setter
public class ProductOptionXref extends BaseEntity {
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "product_id")
  private Product product;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = true)
  @JoinColumn(name = "product_option_id")
  private ProductOption productOption;
}
