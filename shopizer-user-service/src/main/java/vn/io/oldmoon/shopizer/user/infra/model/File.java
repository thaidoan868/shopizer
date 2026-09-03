package vn.io.oldmoon.shopizer.user.infra.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.io.oldmoon.shopizer.common.core.constant.MediaType;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

public class File extends BaseEntity {
  @NotBlank private String bucket;

  @NotBlank private String objectName;

  private Long sizeBytes;

  @Enumerated(EnumType.STRING)
  private MediaType contentType;

  @Enumerated(EnumType.STRING)
  private Visibility visibility;
}
