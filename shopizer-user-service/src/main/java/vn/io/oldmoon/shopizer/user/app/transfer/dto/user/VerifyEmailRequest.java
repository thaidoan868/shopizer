package vn.io.oldmoon.shopizer.user.app.transfer.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class VerifyEmailRequest {
  @Schema(example = "alice1782@domain.com")
  @Email
  @NotBlank(message = "Email is required")
  @Length(max = 100, message = "Email must be 100 characters or fewer")
  private String email;

  @Schema(description = "A six digit code", example = "587462")
  @Pattern(regexp = "^\\d{6}$", message = "Code must contain exactly 6 digits")
  private String code;
}
