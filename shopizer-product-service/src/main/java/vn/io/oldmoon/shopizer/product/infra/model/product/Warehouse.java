package vn.io.oldmoon.shopizer.product.infra.model.product;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.io.oldmoon.shopizer.common.core.constant.Address;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "warehouses")
public class Warehouse extends BaseEntity {

  private String code;

  private String name;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Address location;

  @Builder.Default private boolean deleted = false;
}
