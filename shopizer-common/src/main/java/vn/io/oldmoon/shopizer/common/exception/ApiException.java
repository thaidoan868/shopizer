package vn.io.oldmoon.shopizer.common.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
  private final ErrorCode errorCode;
  private final String message;
  private final List<String> errors;

  public ApiException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.message = message;
    this.errors = null;
  }
}
