package vn.io.oldmoon.shopizer.common.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.core.exception.AuthenticationException;
import vn.io.oldmoon.shopizer.common.core.exception.DuplicateResourceException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.UnauthorizedActionException;
import vn.io.oldmoon.shopizer.common.web.model.ErrorResponse;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApplicationExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      ApiException ex, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse(
            ex.getErrorCode().getError(), ex.getMessage(), ex.getErrors(), request.getRequestURI());
    log.error("ApiException", ex);

    return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), ex.getMessage(), request.getRequestURI());
    log.error("AuthenticationException", ex);

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.NOT_FOUND;
    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), ex.getMessage(), request.getRequestURI());
    log.error("ResourceNotFoundException", ex);

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException ex, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.CONFLICT;
    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), ex.getMessage(), request.getRequestURI());
    log.error("DuplicateResourceException", ex);

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<ErrorResponse> handleInvalidInputException(
      InvalidInputException ex, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.BAD_REQUEST;
    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), ex.getMessage(), request.getRequestURI());
    log.error("InvalidInputException", ex);

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(UnauthorizedActionException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedActionException(
      UnauthorizedActionException ex, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.FORBIDDEN;
    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), ex.getMessage(), request.getRequestURI());
    log.error("UnauthorizedActionException", ex);

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }
}
