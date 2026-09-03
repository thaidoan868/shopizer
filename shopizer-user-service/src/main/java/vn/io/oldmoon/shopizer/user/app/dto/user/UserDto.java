package vn.io.oldmoon.shopizer.user.app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

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

  private AvatarDto avatarMeta;
}
