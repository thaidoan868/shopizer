package vn.io.oldmoon.shopizer.common.core.exception;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiException extends FatalException {
  private final ErrorCode errorCode;
  private final List<String> errors;

  public ApiException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.errors = null;
  }

  public ApiException(ErrorCode errorCode, String message, List<String> errors) {
    super(message);
    this.errorCode = errorCode;
    this.errors = errors;
  }
}
