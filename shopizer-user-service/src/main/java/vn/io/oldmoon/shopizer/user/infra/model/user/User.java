package vn.io.oldmoon.shopizer.user.infra.model.user;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
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
@Table(name = "users")
public class User extends BaseEntity {
  @NotBlank @Builder.Default private String realm = "shopizer";

  @NotNull private UUID keycloakUserId;

  @NotBlank private String username;

  @NotBlank private String email;

  @NotNull @Builder.Default private Boolean verified = Boolean.FALSE;

  @NotNull @Builder.Default private String firstName = "";

  @NotNull @Builder.Default private String lastName = "";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Valid
  private AvatarMeta avatarMeta;

  public String getFullName() {
    return firstName + " " + lastName;
  }
}
