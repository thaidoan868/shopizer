package vn.io.oldmoon.shopizer.product.infra.model.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

  @Column(nullable = false)
  @NotBlank
  private String name;

  @Column(unique = true, nullable = false)
  @NotBlank
  private String slug;

  @Column(length = 100)
  private String brand;

  @Column(columnDefinition = "text")
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Builder.Default
  private List<Attribute> attributes = new ArrayList<>();

  @Builder.Default
  @Column(name = "is_serialized", nullable = false)
  @NotNull
  private Boolean serialized = false;

  @Enumerated(EnumType.STRING)
  @Column(length = 25, nullable = false)
  @NotNull
  @Builder.Default
  private ProductStatus status = ProductStatus.ACTIVE;

  @Builder.Default
  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @ToString.Exclude
  private List<Sku> skus = new ArrayList<>();
}
