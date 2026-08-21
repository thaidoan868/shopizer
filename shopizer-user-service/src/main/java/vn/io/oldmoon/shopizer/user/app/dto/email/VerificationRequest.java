package vn.io.oldmoon.shopizer.user.app.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class VerificationRequest {
  @Schema(example = "alice1782@domain.com")
  @Email
  @NotBlank(message = "Email is required")
  @Length(max = 100, message = "Email must be 100 characters or fewer")
  private String email;
}
