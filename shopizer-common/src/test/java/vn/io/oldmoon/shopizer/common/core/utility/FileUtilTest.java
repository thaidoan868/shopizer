package vn.io.oldmoon.shopizer.common.core.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class FileUtilTest {

  @Test
  @DisplayName("extractFilename should extract filename with white spaces and special characters")
  void extractFilename_WithWhiteSpacesAndSpecialChars_ShouldExtractFilename() {
    String unixPath = "/uploads/2026/my new picture (draft #1) [final].jpg";
    assertThat(FileUtil.extractFilename(unixPath)).isEqualTo("my new picture (draft #1) [final].jpg");

    String windowsPath = "C:\\Users\\Admin\\Pictures\\camera raw & edit #2!@.png";
    assertThat(FileUtil.extractFilename(windowsPath)).isEqualTo("camera raw & edit #2!@.png");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("extractFilename should return input when null or empty")
  void extractFilename_WhenNullOrEmpty_ShouldReturnSame(String input) {
    assertThat(FileUtil.extractFilename(input)).isEqualTo(input);
  }

  @Test
  @DisplayName("encodeForUrl should encode white spaces and special characters properly")
  void encodeForUrl_WithWhiteSpacesAndSpecialChars_ShouldEncodeProperly() {
    String filename = "my image (2024) [v1].png";
    String encoded = FileUtil.encodeForUrl(filename);

    assertThat(encoded).isEqualTo("my%20image%20%282024%29%20%5Bv1%5D.png");
    assertThat(encoded).doesNotContain(" ");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("encodeForUrl should return input when null or empty")
  void encodeForUrl_WhenNullOrEmpty_ShouldReturnSame(String input) {
    assertThat(FileUtil.encodeForUrl(input)).isEqualTo(input);
  }

  @Test
  @DisplayName("deleteTempFileQuietly should delete existing file and ignore null or missing path")
  void deleteTempFileQuietly_ShouldDeleteFileAndIgnoreNull(@TempDir Path tempDir) throws Exception {
    assertThatCode(() -> FileUtil.deleteTempFileQuietly(null)).doesNotThrowAnyException();

    Path tempFile = tempDir.resolve("temp.txt");
    Files.writeString(tempFile, "hello");
    assertThat(Files.exists(tempFile)).isTrue();

    FileUtil.deleteTempFileQuietly(tempFile);
    assertThat(Files.exists(tempFile)).isFalse();
  }
}
