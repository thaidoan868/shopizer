package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Deprecated
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class ProductOptionValue extends BaseEntity {
  private String value;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_option_id")
  private ProductOption productOption;

  private Integer displayOrder;
  private Boolean deleted;
}
