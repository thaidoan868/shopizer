package vn.io.oldmoon.shopizer.common.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.parser.Parser;

/**
 * Constraint validator implementation for {@link WellFormedHtml} backed by Jsoup parser.
 * Checks whether the HTML string is parseable and well-formed without syntax/tree-building errors.
 */
public class WellFormedHtmlValidator implements ConstraintValidator<WellFormedHtml, String> {

  private static final int MAX_TRACKED_ERRORS = 1;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }

    try {
      Parser parser = Parser.htmlParser().setTrackErrors(MAX_TRACKED_ERRORS);
      parser.parseInput(value, "");
      return parser.getErrors().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }
}
