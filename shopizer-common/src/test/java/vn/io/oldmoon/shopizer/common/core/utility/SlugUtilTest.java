package vn.io.oldmoon.shopizer.common.core.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlugUtilTest {

  @Test
  @DisplayName("generateSlug should trim and convert explicit slug to lower case")
  void generateSlug_WhenExplicitSlugProvided_ShouldNormalize() {
    String result = SlugUtil.generateSlug("  RTX-4090-Review  ", "Fallback Title");
    assertThat(result).isEqualTo("rtx-4090-review");
  }

  @Test
  @DisplayName("generateSlug should auto-generate slug with UUID suffix when explicit slug is null")
  void generateSlug_WhenSlugIsNull_ShouldGenerateFromName() {
    String result = SlugUtil.generateSlug(null, "RTX 4090 Gaming Performance");
    assertThat(result).startsWith("rtx-4090-gaming-performance-");
    assertThat(result.length()).isEqualTo("rtx-4090-gaming-performance-".length() + 8);
  }

  @Test
  @DisplayName("generateSlug should auto-generate slug with UUID suffix when explicit slug is blank")
  void generateSlug_WhenSlugIsBlank_ShouldGenerateFromName() {
    String result = SlugUtil.generateSlug("   ", "Intel Core i9 14900K");
    assertThat(result).startsWith("intel-core-i9-14900k-");
  }

  @Test
  @DisplayName("generateSlug should return original slug when both are null")
  void generateSlug_WhenBothNull_ShouldReturnNull() {
    String result = SlugUtil.generateSlug(null, null);
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("generateSlug should return original slug when both are blank")
  void generateSlug_WhenBothBlank_ShouldReturnOriginalSlug() {
    String result = SlugUtil.generateSlug("  ", "  ");
    assertThat(result).isEqualTo("  ");
  }
}
