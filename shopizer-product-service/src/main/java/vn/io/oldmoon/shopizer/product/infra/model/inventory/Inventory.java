package vn.io.oldmoon.shopizer.product.infra.model.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Generated;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.product.infra.model.product.Sku;
import vn.io.oldmoon.shopizer.product.infra.model.product.Warehouse;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sku_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Sku sku;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Warehouse warehouse;

  @Column(name = "quantity_total", nullable = false)
  @NotNull
  @Builder.Default
  private Integer quantityTotal = 0;

  @Column(name = "quantity_defective", nullable = false)
  @NotNull
  @Builder.Default
  private Integer quantityDefective = 0;

  @Column(name = "quantity_reserved", nullable = false)
  @NotNull
  @Builder.Default
  private Integer quantityReserved = 0;

  @Column(name = "quantity_sold", nullable = false)
  @NotNull
  @Builder.Default
  private Integer quantitySold = 0;

  @Column(name = "quantity_returned", nullable = false)
  @NotNull
  @Builder.Default
  private Integer quantityReturned = 0;

  // Hibernate will automatically fetch this generated column after inserts/updates
  @Generated
  @Column(name = "quantity_available", insertable = false, updatable = false)
  private Integer quantityAvailable;
}
