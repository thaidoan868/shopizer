package vn.io.oldmoon.shopizer.user.infra.model.profile;

import jakarta.persistence.*;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "employee_profiles")
public class EmployeeProfile extends BaseEntity {
  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE})
  @JoinColumn(name = "user_id")
  @ToString.Exclude
  private User user;

  @Enumerated(EnumType.STRING)
  private Shift shift;

  private String workPhone;
}
