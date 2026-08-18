package vn.io.oldmoon.shopizer.user.business.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UrlConvertService {

  private final String assetBaseUrl;

  public UrlConvertService(@Value("${minio.endpoint}") String assetBaseUrl) {
    this.assetBaseUrl = assetBaseUrl;
  }

  public String media(String bucket, String objectName) {
    return UriComponentsBuilder.fromHttpUrl(assetBaseUrl)
        .pathSegment(bucket, objectName)
        .build()
        .toUriString();
  }
}
