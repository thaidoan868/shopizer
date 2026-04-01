package vn.io.oldmoon.shopizer.common.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.web.model.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class BusinessExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      ApiException ex, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse(
            ex.getErrorCode().getError(), ex.getMessage(), ex.getErrors(), request.getRequestURI());
    log.error("ApiException", ex);

    return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
  }
}
