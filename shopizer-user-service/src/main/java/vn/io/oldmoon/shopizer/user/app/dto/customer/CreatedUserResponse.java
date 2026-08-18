package vn.io.oldmoon.shopizer.user.app.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;

@Data
public class CreatedUserResponse {
  @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @Schema(example = "aliceambrason1782")
  private String username;

  @Schema(example = "alice1782@domain.com")
  private String email;

  @Schema(example = "Alice")
  private String firstName;

  @Schema(example = "Queen")
  private String lastName;
}
