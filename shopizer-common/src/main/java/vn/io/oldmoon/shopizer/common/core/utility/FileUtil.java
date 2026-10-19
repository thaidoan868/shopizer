package vn.io.oldmoon.shopizer.common.core.utility;

/**
 * Utility class for file operations, filename extraction, and URL encoding.
 */
public final class FileUtil {

  private FileUtil() {}

  /**
   * Extracts the simple filename from a potentially full path (handles both Unix and Windows separators).
   *
   * @param filename the raw filename or path
   * @return the extracted filename, or null if input is null
   */
  public static String extractFilename(String filename) {
    return ConverterUtil.extractFilename(filename);
  }

  /**
   * Encodes a string value for safe inclusion in a URL path segment.
   * Replaces '+' with '%20' for RFC 3986 compliance.
   *
   * @param value the string to encode
   * @return the URL-encoded string, or null if input is null
   */
  public static String encodeForUrl(String value) {
    return ConverterUtil.encodeForUrl(value);
  }
}
