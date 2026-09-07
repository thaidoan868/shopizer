package vn.io.oldmoon.shopizer.common.core.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConverterUtilTest {

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
