package vn.io.oldmoon.shopizer.product.infra.model.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "attribute_templates")
public class AttributeTemplate extends BaseEntity {

  @NotBlank private String name;

  @Column(length = 50)
  @NotBlank
  private String code;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private List<Attribute> attributes = new ArrayList<>();
}
