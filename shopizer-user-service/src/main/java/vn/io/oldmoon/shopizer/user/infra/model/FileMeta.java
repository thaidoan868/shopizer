package vn.io.oldmoon.shopizer.user.infra.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;
import vn.io.oldmoon.shopizer.user.infra.data.constant.FileStatus;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "files")
public class FileMeta extends BaseEntity {

  @NotBlank private String bucket;

  @NotBlank private String objectName;

  private Long sizeBytes;

  private String contentType;

  @Enumerated(EnumType.STRING)
  private Visibility visibility;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private FileStatus status = FileStatus.ACTIVE;
}
