package vn.io.oldmoon.shopizer.common.core.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for file operations, filename extraction, and URL encoding.
 */
@Slf4j
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

  /**
   * Deletes a temporary file if it exists, logging any I/O error without throwing.
   *
   * @param path the path to delete
   */
  public static void deleteTempFileQuietly(Path path) {
    if (path != null) {
      try {
        Files.deleteIfExists(path);
      } catch (IOException e) {
        log.warn("Failed to delete temporary file: {}", path, e);
      }
    }
  }
}
