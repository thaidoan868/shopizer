package vn.io.oldmoon.shopizer.user.app.system.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.oldmoon.shopizer.user.app.dto.error.ErrorResponse;
import vn.io.oldmoon.shopizer.user.business.exception.BusinessException;
import vn.io.oldmoon.shopizer.user.business.exception.ErrorCode;

@RestControllerAdvice
@Slf4j
public class BusinessExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      BusinessException ex, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse(
            ex.getErrorCode().getError(), ex.getMessage(), ex.getErrors(), request.getRequestURI());
    log.error("BusinessException", ex);

    return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
  }

  // @valid exception
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .toList();

    String message = "Validation failed with " + errors.size() + " error(s).";
    ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), message, errors, request.getRequestURI());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }
}
