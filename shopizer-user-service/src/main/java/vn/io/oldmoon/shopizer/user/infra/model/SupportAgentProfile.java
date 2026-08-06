package vn.io.oldmoon.shopizer.user.infra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
public class SupportAgentProfile extends BaseEntity {
  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinColumn(name = "user_id")
  @ToString.Exclude
  private User user;

  @NotNull private UUID keycloakUserId;

  @Enumerated(EnumType.STRING)
  private Shift shift;

  private String workPhone;
  private String supportPhone;
}
