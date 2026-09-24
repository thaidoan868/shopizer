package vn.io.oldmoon.shopizer.product.infra.model.product;

import jakarta.persistence.*;
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
@Table(name = "system_dictionaries")
public class SystemDictionary extends BaseEntity {

  @Column(unique = true, nullable = false, length = 50)
  private String code;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private List<String> dictValues;
}
