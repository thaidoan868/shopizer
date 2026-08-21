package vn.io.oldmoon.shopizer.user.infra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.io.oldmoon.shopizer.common.core.constant.MediaType;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Medium extends BaseEntity {
  @NotBlank private String bucket;

  @NotBlank private String objectName;

  @Enumerated(EnumType.STRING)
  private Visibility visibility;

  private String title;

  private String altText;

  @NotNull
  @Enumerated(EnumType.STRING)
  private MediaType contentType;

  private Long sizeBytes;
}
