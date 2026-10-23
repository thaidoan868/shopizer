package vn.io.oldmoon.shopizer.product.infra.model.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.io.oldmoon.shopizer.common.web.model.BaseEntity;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "image_assets")
public class ImageAsset extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "original_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private File original;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "thumbnail_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private File thumbnail;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "medium_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private File medium;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "large_id", nullable = false)
  @ToString.Exclude
  @NotNull
  private File large;
}
