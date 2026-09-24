package vn.io.oldmoon.shopizer.product.infra.model.media;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "articles")
public class Article extends BaseEntity {

  @Column(nullable = false)
  @NotBlank
  private String title;

  @Column(unique = true, nullable = false)
  @NotBlank
  private String slug;

  @Column(name = "content_html", columnDefinition = "text")
  private String contentHtml;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @NotNull
  @Builder.Default
  private ArticleStatus status = ArticleStatus.DRAFT;
}
