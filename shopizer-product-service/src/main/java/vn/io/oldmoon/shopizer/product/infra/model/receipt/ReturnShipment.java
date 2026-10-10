package vn.io.oldmoon.shopizer.product.infra.model.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "return_shipments")
public class ReturnShipment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Warehouse warehouse;

  @Column(name = "vendor_name", nullable = false)
  @NotBlank
  private String vendorName;

  @Column(columnDefinition = "text", nullable = false)
  @NotBlank
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull
  @Builder.Default
  private ShipmentStatus status = ShipmentStatus.DRAFT;

  @Builder.Default
  @OneToMany(mappedBy = "returnShipment", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<ReturnShipmentLine> lines = new ArrayList<>();
}
