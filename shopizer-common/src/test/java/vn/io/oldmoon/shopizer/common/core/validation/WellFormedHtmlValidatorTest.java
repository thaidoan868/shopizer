package vn.io.oldmoon.shopizer.common.core.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class WellFormedHtmlValidatorTest {

  private final WellFormedHtmlValidator validator = new WellFormedHtmlValidator();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("isValid should return true for null, empty, or blank strings")
  void isValid_WhenNullOrBlank_ShouldReturnTrue(String input) {
    assertThat(validator.isValid(input, null)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "<p>Simple paragraph</p>",
        "<p>Paragraph with <br> break and text</p>",
        "<p>Paragraph with <img src=\"test.jpg\" alt=\"test\"> image</p>",
        "<a href=\"https://test.com\" target=\"_blank\">link</a>",
        "<h2>Heading</h2><p>Text</p>",
        "<p style=\"text-align: center; color: red;\">Styled text</p>",
        "<ul><li>Item 1</li><li>Item 2</li></ul>",
        "<ol><li>Item 1</li><li>Item 2</li></ol>",
        "<table><thead><tr><th>Header</th></tr></thead><tbody><tr><td>Data</td></tr></tbody></table>",
        "<blockquote>Quote</blockquote>",
        "<pre><code class=\"language-java\">System.out.println(\"hello\");</code></pre>",
        "<p>&nbsp;Special &amp; entities &lt;3</p>",
        "<p><br></p>",
        "<figure><img src=\"test.jpg\"><figcaption>Caption</figcaption></figure>"
      })
  @DisplayName("isValid should return true for well-formed WYSIWYG HTML fragments")
  void isValid_WithWellFormedWysiwygHtml_ShouldReturnTrue(String html) {
    assertThat(validator.isValid(html, null)).isTrue();
  }

  @Test
  @DisplayName("isValid should return false for malformed HTML with invalid nesting")
  void isValid_WithMalformedHtml_ShouldReturnFalse() {
    String malformed = "<p>Text <div>Invalid block inside inline</p></div>";
    assertThat(validator.isValid(malformed, null)).isFalse();
  }
}
