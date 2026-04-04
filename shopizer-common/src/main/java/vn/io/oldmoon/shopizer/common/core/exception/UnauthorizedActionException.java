package vn.io.oldmoon.shopizer.common.core.exception;

// Throw this when the user is logged in but does not have permission for a specific resource.
public class UnauthorizedActionException extends ServiceException {
  public UnauthorizedActionException(String message) {
    super(message);
  }
}
