package vn.io.oldmoon.shopizer.user.app.transfer.populator.url;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UrlConverter {

  private final String assetBaseUrl;

  public UrlConverter(@Value("${minio.endpoint}") String assetBaseUrl) {
    this.assetBaseUrl = assetBaseUrl;
  }

  public String media(String bucket, String objectName) {
    return UriComponentsBuilder.fromHttpUrl(assetBaseUrl)
        .pathSegment(bucket, objectName)
        .build()
        .toUriString();
  }
}
