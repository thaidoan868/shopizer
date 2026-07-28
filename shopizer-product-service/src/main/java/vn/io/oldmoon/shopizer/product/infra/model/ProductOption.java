package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.*;
import java.util.*;
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
public class ProductOption extends BaseEntity {
  @OneToMany(
      mappedBy = "productOption",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = false)
  @ToString.Exclude
  private Set<ProductOptionValue> values = new HashSet<>();

  private String name;
  private String label;
  private String description;
  @Builder.Default private Boolean required = true;
  private Integer displayOrder;
  @Builder.Default private Boolean deleted = false;
}
