package vn.io.oldmoon.shopizer.common.core.utility;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.util.UriComponentsBuilder;

public class ConverterUtil {

  private ConverterUtil() {}

  public static String toMediaUrl(String mediaEndpoint, String bucket, String objectName) {
    return UriComponentsBuilder.fromHttpUrl(mediaEndpoint)
        .pathSegment(bucket, objectName)
        .build()
        .toUriString();
  }

  /**
   * Encodes a string value for safe inclusion in a URL path segment.
   * Replaces '+' with '%20' for RFC 3986 compliance.
   *
   * @param value the string to encode
   * @return the URL-encoded string, or null if input is null
   */
  public static String encodeForUrl(String value) {
    if (value == null) {
      return null;
    }
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  /**
   * Extracts the simple filename from a potentially full path (handles both Unix and Windows separators).
   *
   * @param filename the raw filename or path
   * @return the extracted filename, or null if input is null
   */
  public static String extractFilename(String filename) {
    if (filename == null) {
      return null;
    }
    int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
    if (lastSlash >= 0) {
      return filename.substring(lastSlash + 1);
    }
    return filename;
  }
}
