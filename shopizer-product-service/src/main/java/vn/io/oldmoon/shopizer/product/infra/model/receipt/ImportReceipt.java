package vn.io.oldmoon.shopizer.product.infra.model.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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
@Table(name = "import_receipts")
public class ImportReceipt extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Warehouse warehouse;

  @Column(name = "supplier_name", nullable = false)
  @NotBlank
  private String supplierName;

  @Column(name = "reference_code", length = 100)
  private String referenceCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull
  @Builder.Default
  private ReceiptStatus status = ReceiptStatus.DRAFT;

  @Column(name = "received_at")
  private Instant receivedAt;

  @Builder.Default
  @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<ImportReceiptLine> lines = new ArrayList<>();
}
