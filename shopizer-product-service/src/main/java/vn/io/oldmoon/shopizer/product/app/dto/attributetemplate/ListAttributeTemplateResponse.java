package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAttributeTemplateResponse {

  @Schema(description = "List of attribute templates")
  @JsonAlias({"items", "attributeTemplates", "data"})
  private List<ShortAttributeTemplateResponse> templates;

  @Schema(example = "10", description = "Total count of templates")
  @JsonAlias({"total", "count", "total_count"})
  private long totalCount;

  public ListAttributeTemplateResponse(List<ShortAttributeTemplateResponse> templates) {
    this.templates = templates;
    this.totalCount = templates != null ? templates.size() : 0;
  }

  @JsonIgnore
  public long getTotal() {
    return totalCount;
  }

  public void setTotal(long total) {
    this.totalCount = total;
  }
}
