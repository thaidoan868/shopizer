package vn.io.oldmoon.shopizer.product.infra.model.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(
    name = "product_files",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "file_id", "type"}))
public class ProductFile extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "file_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private File file;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull
  private ProductFileType type;

  @Column(name = "display_order", nullable = false)
  @NotNull
  @Builder.Default
  private Integer displayOrder = 0;
}
