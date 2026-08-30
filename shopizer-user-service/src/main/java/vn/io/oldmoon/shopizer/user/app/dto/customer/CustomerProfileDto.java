package vn.io.oldmoon.shopizer.user.app.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileDto {
  @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID keycloakUserId;

  @Schema(example = "aliceambrason1782")
  private String username;

  @Schema(example = "alice1782@domain.com")
  private String email;

  @Schema(example = "Alice")
  private String firstName;

  @Schema(example = "Queen")
  private String lastName;

  @Schema(example = "false")
  private Boolean verified;

  @Schema(example = "male")
  private Gender gender;

  @Schema(example = "2003-01-30")
  private LocalDate dateOfBirth;

  @Schema(example = "en")
  private Language language;

  @Schema(example = "+84901234567")
  private String phoneNumber;

  @Schema(example = "No. 123 Ly Thai To, Phuong Hoa Hung, Tp HCM, VN")
  private String address;

  private AvatarDto avatarMeta;
}
