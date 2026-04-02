package vn.io.oldmoon.shopizer.common.core.exception;

public class ServiceException extends FatalException {
  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceException(Throwable cause) {
    super(cause);
  }
}
