package vn.io.oldmoon.shopizer.user.app.dto.customer.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PublicCustomerProfileResponse {
  @Schema(example = "aliceambrason1782")
  private String username;

  @Schema(example = "Alice")
  private String firstName;

  @Schema(example = "Queen")
  private String lastName;

  private AvatarResponse avatarMeta;
}
