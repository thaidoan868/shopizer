package vn.io.oldmoon.shopizer.user.app.dto.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;

class UrlConvertServiceTest {
  private UrlConvertService urlConvertService;

  @Test
  void shouldConvertToMediaUrl() {
    // given
    String baseUrl = "http://localhost:8080/";
    String bucket = "testbucket";
    String objectName = "testobject";
    urlConvertService = new UrlConvertService(baseUrl);

    // when
    String result = urlConvertService.media(bucket, objectName);

    // then
    assertThat(result).isEqualTo("http://localhost:8080/testbucket/testobject");
  }
}
