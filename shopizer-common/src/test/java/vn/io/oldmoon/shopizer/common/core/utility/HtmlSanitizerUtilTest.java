package vn.io.oldmoon.shopizer.common.core.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HtmlSanitizerUtilTest {

  @Test
  @DisplayName("sanitize should return null when html input is null")
  void sanitize_WhenHtmlIsNull_ShouldReturnNull() {
    assertThat(HtmlSanitizerUtil.sanitize(null)).isNull();
  }

  @Test
  @DisplayName("sanitize should preserve rich text elements from WYSIWYG editor")
  void sanitize_WithWysiwygRichText_ShouldPreserveAllowedTags() {
    String wysiwygHtml =
        "<h1>Title</h1>"
            + "<p>Paragraph with <strong>bold</strong>, <em>italic</em>, and <a href=\"https://example.com\">link</a>.</p>"
            + "<ul><li>List item 1</li><li>List item 2</li></ul>"
            + "<blockquote>A helpful quote</blockquote>"
            + "<img src=\"https://example.com/gpu.png\" alt=\"GPU Image\" />"
            + "<table><thead><tr><th>Column</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody></table>";

    String sanitized = HtmlSanitizerUtil.sanitize(wysiwygHtml);

    assertThat(sanitized).contains("<h1>Title</h1>");
    assertThat(sanitized).contains("<strong>bold</strong>");
    assertThat(sanitized).contains("<em>italic</em>");
    assertThat(sanitized).contains("<a href=\"https://example.com\" rel=\"nofollow\">link</a>");
    assertThat(sanitized).contains("<ul><li>List item 1</li><li>List item 2</li></ul>");
    assertThat(sanitized).contains("<blockquote>A helpful quote</blockquote>");
    assertThat(sanitized).contains("<img src=\"https://example.com/gpu.png\" alt=\"GPU Image\" />");
    assertThat(sanitized).contains("<table><thead><tr><th>Column</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody></table>");
  }

  @Test
  @DisplayName("sanitize should preserve safe inline styles")
  void sanitize_WithSafeStyles_ShouldPreserveStyles() {
    String styledHtml = "<p style=\"text-align: center; color: red;\">Centered colored text</p>";
    String sanitized = HtmlSanitizerUtil.sanitize(styledHtml);

    assertThat(sanitized).contains("style=\"text-align:center;color:red\"");
    assertThat(sanitized).contains("Centered colored text");
  }

  @Test
  @DisplayName("sanitize should strip script tags and inline event handlers to prevent XSS")
  void sanitize_WithXssPayload_ShouldStripMaliciousContent() {
    String xssHtml =
        "<p>Normal text</p>"
            + "<script>alert('xss attack');</script>"
            + "<img src=\"x\" onerror=\"alert('hacked')\" />"
            + "<a href=\"javascript:alert('pwned')\">Click here</a>";

    String sanitized = HtmlSanitizerUtil.sanitize(xssHtml);

    assertThat(sanitized).contains("<p>Normal text</p>");
    assertThat(sanitized).doesNotContain("<script>");
    assertThat(sanitized).doesNotContain("alert('xss attack')");
    assertThat(sanitized).doesNotContain("onerror");
    assertThat(sanitized).doesNotContain("javascript:");
  }
}
