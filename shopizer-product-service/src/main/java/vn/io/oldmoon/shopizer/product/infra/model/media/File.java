package vn.io.oldmoon.shopizer.product.infra.model.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(
    name = "files",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bucket", "object_name"}))
public class File extends BaseEntity {

  @Column(nullable = false)
  @NotBlank
  private String bucket;

  @Column(name = "object_name", nullable = false)
  @NotBlank
  private String objectName;

  @Column(name = "size_bytes")
  @PositiveOrZero
  private Long sizeBytes;

  @Column(name = "content_type", length = 50)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private Visibility visibility;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  @Builder.Default
  private FileStatus status = FileStatus.ACTIVE;
}
