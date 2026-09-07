package vn.io.oldmoon.shopizer.common.core.utility;

import org.springframework.web.util.UriComponentsBuilder;

public class ConverterUtil {
  public static String toMediaUrl(String mediaEndpoint, String bucket, String objectName) {
    return UriComponentsBuilder.fromHttpUrl(mediaEndpoint)
        .pathSegment(bucket, objectName)
        .build()
        .toUriString();
  }
}
