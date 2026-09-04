package vn.io.oldmoon.shopizer.user.infra.model.profile;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

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
      cascade = {CascadeType.MERGE})
  @JoinColumn(name = "user_id")
  @ToString.Exclude
  @NotNull
  private User user;

  private String phoneNumber;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Valid
  private Address address;

  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  private Gender gender;
}
