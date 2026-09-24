package vn.io.oldmoon.shopizer.product.infra.model.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
@Table(
    name = "inventory_serials",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sku_id", "serial_number"}))
public class InventorySerial extends BaseEntity {

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

  @Column(name = "serial_number", nullable = false)
  @NotBlank
  private String serialNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull
  private InventorySerialStatus status;
}
