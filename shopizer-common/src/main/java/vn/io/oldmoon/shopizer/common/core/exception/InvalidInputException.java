package vn.io.oldmoon.shopizer.common.core.exception;

// Throw this when the data fails business basic rules.
// e.g., negative age, empty name
public class InvalidInputException extends ServiceException {
  public InvalidInputException(String message) {
    super(message);
  }
}
