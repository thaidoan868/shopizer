package vn.io.oldmoon.shopizer.product.infra.model.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.product.infra.model.product.Sku;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "internal_outbound_lines")
public class InternalOutboundLine extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shipment_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private InternalOutboundShipment shipment;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sku_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Sku sku;

  @Column(nullable = false)
  @NotNull
  @Positive
  private Integer quantity;

  @Builder.Default
  @OneToMany(mappedBy = "internalOutboundLine", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<InternalOutboundLineSerial> serials = new ArrayList<>();
}
