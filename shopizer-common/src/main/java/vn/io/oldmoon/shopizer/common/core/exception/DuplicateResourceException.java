package vn.io.oldmoon.shopizer.common.core.exception;

/** Throw this when a unique constraint is violated at the business level. */
// The data is "fine," but someone else got there first.
public class DuplicateResourceException extends ServiceException {
  public DuplicateResourceException(String message) {
    super(message);
  }
}
