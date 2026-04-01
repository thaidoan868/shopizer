package vn.io.oldmoon.shopizer.user.app.transfer.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailVerificationCodeRequest {
  @Schema(
      description = "Username must be at least 3 characters and contain no whitespace",
      example = "aliceambrason1782")
  @NotBlank(message = "Username must not be blank")
  @Length(min = 3, max = 50, message = "Username must be at least 3 characters")
  @Pattern(regexp = "^\\S+$", message = "Username must not contain whitespace")
  private String username;

  @Schema(example = "alice1782@domain.com")
  @Email
  @NotBlank(message = "Email is required")
  @Length(max = 100, message = "Email must be 100 characters or fewer")
  private String email;
}
