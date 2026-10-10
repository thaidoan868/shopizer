package vn.io.oldmoon.shopizer.product.infra.model.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
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
@Table(name = "inventory_serial_transactions")
public class InventorySerialTransaction extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sku_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Sku sku;

  @Column(name = "serial_number", nullable = false)
  @NotBlank
  private String serialNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Warehouse warehouse;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 50)
  @NotNull
  private TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_change", nullable = false, length = 50)
  @NotNull
  private InventorySerialStatus statusChange;

  @Column(name = "reference_type", nullable = false, length = 50)
  @NotBlank
  private String referenceType;

  @Column(name = "reference_id", nullable = false)
  @NotNull
  private UUID referenceId;
}
