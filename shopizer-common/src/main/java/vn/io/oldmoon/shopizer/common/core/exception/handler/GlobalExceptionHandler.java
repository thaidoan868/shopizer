package vn.io.oldmoon.shopizer.common.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.common.web.model.ErrorResponse;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MissingPathVariableException.class)
  public ResponseEntity<ErrorResponse> handleMissingPathVariableException(
      MissingPathVariableException ex, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.BAD_REQUEST;
    String detail = ex.getMessage();
    ErrorResponse body = new ErrorResponse(errorCode.getError(), detail, request.getRequestURI());
    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.BAD_REQUEST;
    String message = "Request body is invalid or unreadable.";
    ErrorResponse body = new ErrorResponse(errorCode.getError(), message, request.getRequestURI());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
    ErrorResponse body =
        new ErrorResponse(errorCode.getError(), e.getMessage(), request.getRequestURI());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundEndpoint(HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.NOT_FOUND;
    String message = "Endpoint does not exist";
    ErrorResponse body = new ErrorResponse(errorCode.getError(), message, request.getRequestURI());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUncaughtException(
      Exception ex, HttpServletRequest request) {
    log.error("Uncaught exception occurred at path {}", request.getRequestURI(), ex);

    ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
    String message = "An unexpected error occurred.";

    ErrorResponse body = new ErrorResponse(errorCode.getError(), message, request.getRequestURI());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }
}
