package vn.io.oldmoon.shopizer.product.infra.model.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(
    name = "internal_outbound_line_serials",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"internal_outbound_line_id", "serial_number"}))
public class InternalOutboundLineSerial extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "internal_outbound_line_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private InternalOutboundLine internalOutboundLine;

  @Column(name = "serial_number", nullable = false)
  @NotBlank
  private String serialNumber;
}
