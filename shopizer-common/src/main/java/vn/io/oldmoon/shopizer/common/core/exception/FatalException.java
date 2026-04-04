package vn.io.oldmoon.shopizer.common.core.exception;

public class FatalException extends RuntimeException {
  public FatalException(String message) {
    super(message);
  }

  public FatalException(String message, Throwable cause) {
    super(message, cause);
  }

  public FatalException(Throwable cause) {
    super(cause);
  }
}
