package vn.io.oldmoon.shopizer.common.core.utility;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/** Utility for HTML sanitization using OWASP Java HTML Sanitizer. */
public final class HtmlSanitizerUtil {

  private HtmlSanitizerUtil() {}

  /**
   * Safe HTML policy allowing standard text formatting, blocks, links, styles, tables, and images,
   * while stripping unsafe tags and scripts to prevent Cross-Site Scripting (XSS).
   */
  public static final PolicyFactory POLICY =
      Sanitizers.FORMATTING
          .and(Sanitizers.BLOCKS)
          .and(Sanitizers.LINKS)
          .and(Sanitizers.STYLES)
          .and(Sanitizers.TABLES)
          .and(Sanitizers.IMAGES);

  /**
   * Sanitizes an HTML string using the configured {@link #POLICY}.
   *
   * @param html the HTML string to sanitize
   * @return the sanitized HTML string, or null if input was null
   */
  public static String sanitize(String html) {
    if (html == null) {
      return null;
    }
    return POLICY.sanitize(html);
  }
}
