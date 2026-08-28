package vn.io.oldmoon.shopizer.user.infra.model.profile;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile extends BaseEntity {
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Language language = Language.en;

  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinColumn(name = "user_id")
  @ToString.Exclude
  @NotNull
  private User user;

  @NotNull private UUID keycloakUserId;
  private String phoneNumber;
  private String address;
  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  private Gender gender;
}
