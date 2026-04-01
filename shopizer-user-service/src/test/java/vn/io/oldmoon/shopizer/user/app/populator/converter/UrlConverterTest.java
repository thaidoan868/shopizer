package vn.io.oldmoon.shopizer.user.app.populator.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import vn.io.oldmoon.shopizer.user.app.transfer.populator.url.UrlConverter;

class UrlConverterTest {
  private UrlConverter urlConverter;

  @Test
  void shouldConvertToMediaUrl() {
    // given
    String baseUrl = "http://localhost:8080/";
    String bucket = "testbucket";
    String objectName = "testobject";
    urlConverter = new UrlConverter(baseUrl);

    // when
    String result = urlConverter.media(bucket, objectName);

    // then
    assertThat(result).isEqualTo("http://localhost:8080/testbucket/testobject");
  }
}
