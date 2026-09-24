package vn.io.oldmoon.shopizer.product.infra.model.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
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
@Table(name = "import_receipt_lines")
public class ImportReceiptLine extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "receipt_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private ImportReceipt receipt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sku_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Sku sku;

  @Column(nullable = false)
  @NotNull
  @Positive
  private Integer quantity;

  @Column(name = "unit_cost", precision = 15, scale = 2)
  @Positive
  private BigDecimal unitCost;

  @Builder.Default
  @OneToMany(mappedBy = "importReceiptLine", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<ImportReceiptLineSerial> serials = new ArrayList<>();
}
