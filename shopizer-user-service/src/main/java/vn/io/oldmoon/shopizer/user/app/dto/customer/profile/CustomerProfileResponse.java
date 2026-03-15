package vn.io.oldmoon.shopizer.user.app.dto.customer.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;

@Data
public class CustomerProfileResponse {
  @Schema(example = "c09974f5-4635-4914-a13c-4477c20ff9b6")
  private UUID userId;

  @Schema(example = "aliceambrason1782")
  private String username;

  @Schema(example = "alice1782@domain.com")
  private String email;

  @Schema(example = "Alice")
  private String firstName;

  @Schema(example = "Queen")
  private String lastName;

  @Schema(example = "male")
  private Gender gender;

  @Schema(example = "2003-1-30")
  private LocalDate dateOfBirth;

  @Schema(example = "en")
  private Language language;

  @Schema(example = "+84901234567")
  private String phoneNumber;

  private AvatarResponse avatarMeta;

  @Schema(example = "No. 123 Ly Thai To, Phuong Hoa Hung, Tp HCM, VN")
  private String address;
}
