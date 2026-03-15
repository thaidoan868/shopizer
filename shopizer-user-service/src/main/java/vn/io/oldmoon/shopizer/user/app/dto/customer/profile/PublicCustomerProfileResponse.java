package vn.io.oldmoon.shopizer.user.app.dto.customer.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;

@Data
public class PublicCustomerProfileResponse {
  @Schema(example = "c09974f5-4635-4914-a13c-4477c20ff9b6")
  private UUID userId;

  @Schema(example = "aliceambrason1782")
  private String username;

  @Schema(example = "Alice")
  private String firstName;

  @Schema(example = "Queen")
  private String lastName;

  private AvatarResponse avatarMeta;
}
