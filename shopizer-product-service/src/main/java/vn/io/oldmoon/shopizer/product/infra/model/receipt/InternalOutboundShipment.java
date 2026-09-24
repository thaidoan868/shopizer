package vn.io.oldmoon.shopizer.product.infra.model.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.product.infra.model.product.Warehouse;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "internal_outbound_shipments")
public class InternalOutboundShipment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_warehouse_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Warehouse sourceWarehouse;

  // Nullable, as items can be consumed internally
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "destination_warehouse_id")
  @ToString.Exclude
  private Warehouse destinationWarehouse;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull
  @Builder.Default
  private ShipmentStatus status = ShipmentStatus.DRAFT;

  @Builder.Default
  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<InternalOutboundLine> lines = new ArrayList<>();
}
