package vn.io.oldmoon.shopizer.common.core.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ConverterUtilTest {

  @Nested
  @DisplayName("toMediaUrl(String mediaEndpoint, String bucket, String objectName)")
  class ToMediaUrlTests {

    @Test
    @DisplayName("toMediaUrl should convert mediaEndpoint, bucket, and objectName to media URL")
    void toMediaUrl_ShouldConvertToMediaUrl() {
      String mediaEndpoint = "http://localhost:8080";
      String bucket = "testbucket";
      String objectName = "testobject.png";

      String result = ConverterUtil.toMediaUrl(mediaEndpoint, bucket, objectName);

      assertThat(result).isEqualTo("http://localhost:8080/testbucket/testobject.png");
    }

    @Test
    @DisplayName("toMediaUrl should handle mediaEndpoint with trailing slash")
    void toMediaUrl_WithTrailingSlash_ShouldConvertToMediaUrl() {
      String mediaEndpoint = "http://localhost:8080/";
      String bucket = "testbucket";
      String objectName = "testobject.png";

      String result = ConverterUtil.toMediaUrl(mediaEndpoint, bucket, objectName);

      assertThat(result).isEqualTo("http://localhost:8080/testbucket/testobject.png");
    }
  }

  @Nested
  @DisplayName("encodeForUrl(String value)")
  class EncodeForUrlTests {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should return input directly when null or empty")
    void encodeForUrl_WhenNullOrEmpty_ShouldReturnSame(String input) {
      assertThat(ConverterUtil.encodeForUrl(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("should replace spaces with %20 instead of +")
    void encodeForUrl_WithWhiteSpaces_ShouldEncodeToPercent20() {
      String input = "my test image (2024).jpg";
      String result = ConverterUtil.encodeForUrl(input);

      assertThat(result).isEqualTo("my%20test%20image%20%282024%29.jpg");
    }

    @Test
    @DisplayName("should correctly encode special characters")
    void encodeForUrl_WithSpecialCharacters_ShouldEncodeProperly() {
      String input = "file!@#$%^&*()_+-=[]{}|;':\",.<>?";
      String result = ConverterUtil.encodeForUrl(input);

      assertThat(result).isNotNull();
      assertThat(result).doesNotContain(" ");
      assertThat(result).contains("%21"); // !
      assertThat(result).contains("%40"); // @
      assertThat(result).contains("%23"); // #
      assertThat(result).contains("%24"); // $
      assertThat(result).contains("%25"); // %
    }

    @Test
    @DisplayName("should encode Vietnamese UTF-8 characters with spaces and special characters")
    void encodeForUrl_WithUtf8AndSpecialChars_ShouldEncodeProperly() {
      String input = "hình ảnh sản phẩm (đẹp & chuẩn).png";
      String result = ConverterUtil.encodeForUrl(input);

      assertThat(result).isNotNull();
      assertThat(result).doesNotContain(" ");
      assertThat(result).contains("%20");
    }
  }

  @Nested
  @DisplayName("extractFilename(String filename)")
  class ExtractFilenameTests {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should return input when null or empty")
    void extractFilename_WhenNullOrEmpty_ShouldReturnSame(String input) {
      assertThat(ConverterUtil.extractFilename(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("should return same filename when no slashes are present")
    void extractFilename_WithoutPath_ShouldReturnFilename() {
      String input = "image.png";
      assertThat(ConverterUtil.extractFilename(input)).isEqualTo("image.png");
    }

    @Test
    @DisplayName("should extract filename containing spaces and special characters from Unix path")
    void extractFilename_WithUnixPathAndSpecialChars_ShouldExtractFilename() {
      String input = "/var/data/uploads/my test image (2024) [v1.0] #1.jpg";
      assertThat(ConverterUtil.extractFilename(input))
          .isEqualTo("my test image (2024) [v1.0] #1.jpg");
    }

    @Test
    @DisplayName("should extract filename containing spaces and special characters from Windows path")
    void extractFilename_WithWindowsPathAndSpecialChars_ShouldExtractFilename() {
      String input = "C:\\Users\\John Doe\\Downloads\\special!@#$%^&()_+-=.png";
      assertThat(ConverterUtil.extractFilename(input))
          .isEqualTo("special!@#$%^&()_+-=.png");
    }

    @Test
    @DisplayName("should extract filename from path with mixed slashes")
    void extractFilename_WithMixedSlashes_ShouldExtractFilename() {
      String input = "folder/subfolder\\another file with spaces.jpeg";
      assertThat(ConverterUtil.extractFilename(input))
          .isEqualTo("another file with spaces.jpeg");
    }

    @Test
    @DisplayName("should return empty string when path ends with slash")
    void extractFilename_WithTrailingSlash_ShouldReturnEmptyString() {
      assertThat(ConverterUtil.extractFilename("/path/to/folder/")).isEmpty();
      assertThat(ConverterUtil.extractFilename("C:\\folder\\")).isEmpty();
    }
  }
}
