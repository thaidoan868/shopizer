package vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;

@Data
public class UpdateCustomerProfileRequest {
  @Schema(example = "Alice")
  @Length(max = 50, message = "First name must be 50 characters or fewer")
  private String firstName;

  @Schema(example = "Queen")
  @Length(max = 50, message = "Last name must be 50 characters or fewer")
  private String lastName;

  @Schema(example = "male")
  private Gender gender;

  @Schema(example = "2003-1-30")
  private LocalDate dateOfBirth;

  @Schema(example = "en", defaultValue = "en")
  private Language language;

  @Schema(description = "A valid international phone number", example = "+84901234567")
  @Length(max = 20, message = "Phone Number must be 15 characters or fewer")
  @Pattern(
      regexp = "^\\+?[1-9]\\d{7,14}$",
      message = "Phone number must be a valid international number")
  private String phoneNumber;

  @Schema(example = "No. 123 Ly Thai To, Phuong Hoa Hung, Tp HCM, VN")
  @Length(max = 300, message = "Address must be 300 characters or fewer")
  private String address;
}
