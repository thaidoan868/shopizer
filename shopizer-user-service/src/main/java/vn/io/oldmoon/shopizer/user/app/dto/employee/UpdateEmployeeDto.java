package vn.io.oldmoon.shopizer.user.app.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vn.io.oldmoon.shopizer.user.infra.model.profile.Shift;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeDto {

  @Schema(example = "Alice")
  @Length(max = 50, message = "First name must be 50 characters or fewer")
  private String firstName;

  @Schema(example = "Queen")
  @Length(max = 50, message = "Last name must be 50 characters or fewer")
  private String lastName;

  @Schema(example = "MORNING")
  private Shift shift;

  @Schema(description = "A valid international phone number", example = "+84901234567")
  @Length(max = 20, message = "Phone Number must be 20 characters or fewer")
  @Pattern(
      regexp = "^\\+?[1-9]\\d{7,14}$",
      message = "Phone number must be a valid international number")
  private String workPhone;
}
