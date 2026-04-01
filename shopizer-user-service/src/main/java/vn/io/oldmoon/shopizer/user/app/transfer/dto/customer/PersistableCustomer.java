package vn.io.oldmoon.shopizer.user.app.transfer.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Gender;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;

@Data
public class PersistableCustomer {
  @Schema(
      description = "Username must be at least 3 characters and contain no whitespace",
      example = "aliceambrason1782")
  @NotBlank(message = "Username must not be blank")
  @Length(min = 3, max = 50, message = "Username must be at least 3 characters")
  @Pattern(regexp = "^\\S+$", message = "Username must not contain whitespace")
  private String username;

  @Schema(
      description = "Password must be at least 8 characters and contain no whitespace",
      example = "alicesecretpassword")
  @NotBlank(message = "Password must not be blank")
  @Length(min = 8, message = "Password must be at least 8 characters")
  @Pattern(regexp = "^\\S+$", message = "Password must not contain whitespace")
  private String password;

  @Schema(example = "alice1782@domain.com")
  @Email
  @NotBlank(message = "Email is required")
  @Length(max = 100, message = "Email must be 100 characters or fewer")
  private String email;

  @Schema(example = "Alice")
  @Length(max = 50, message = "First name must be 50 characters or fewer")
  @NotBlank(message = "First Name must not be blank")
  private String firstName;

  @Schema(example = "Queen")
  @Length(max = 50, message = "Last name must be 50 characters or fewer")
  @NotBlank(message = "Last Name must not be blank")
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
  @NotBlank(message = "Phone number must not be blank")
  private String phoneNumber;

  @Schema(example = "No. 123 Ly Thai To, Phuong Hoa Hung, Tp HCM, VN")
  @Length(max = 300, message = "Address must be 300 characters or fewer")
  @NotBlank(message = "Address must not be blank")
  private String address;
}
