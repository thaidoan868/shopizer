package vn.io.oldmoon.shopizer.common.core.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.common.web.model.ErrorResponse;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/users/me/avatar");
  }

  @Test
  @DisplayName("handleMissingServletRequestPartException should return 400 Bad Request with proper ErrorResponse")
  void handleMissingServletRequestPartException_ShouldReturn400() {
    MissingServletRequestPartException ex = new MissingServletRequestPartException("avatar");

    ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestPartException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo(ErrorCode.BAD_REQUEST.getError());
    assertThat(response.getBody().getMessage()).isEqualTo("Required part 'avatar' is not present.");
    assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me/avatar");
    assertThat(response.getBody().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("handleMissingServletRequestParameterException should return 400 Bad Request with proper ErrorResponse")
  void handleMissingServletRequestParameterException_ShouldReturn400() {
    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("param", "String");

    ResponseEntity<ErrorResponse> response =
        handler.handleMissingServletRequestParameterException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo(ErrorCode.BAD_REQUEST.getError());
    assertThat(response.getBody().getMessage())
        .isEqualTo("Required request parameter 'param' for method parameter type String is not present");
    assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me/avatar");
    assertThat(response.getBody().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("handleMultipartException should return 400 Bad Request with proper ErrorResponse")
  void handleMultipartException_ShouldReturn400() {
    MultipartException ex = new MultipartException("Current request is not a multipart request");

    ResponseEntity<ErrorResponse> response = handler.handleMultipartException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo(ErrorCode.BAD_REQUEST.getError());
    assertThat(response.getBody().getMessage()).isEqualTo("Current request is not a multipart request");
    assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me/avatar");
    assertThat(response.getBody().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("handleHttpMediaTypeNotSupported should return 415 Unsupported Media Type with proper ErrorResponse")
  void handleHttpMediaTypeNotSupported_ShouldReturn415() {
    HttpMediaTypeNotSupportedException ex =
        new HttpMediaTypeNotSupportedException("Content-Type 'application/json' is not supported");

    ResponseEntity<ErrorResponse> response = handler.handleHttpMediaTypeNotSupported(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getError());
    assertThat(response.getBody().getMessage())
        .isEqualTo("Content-Type 'application/json' is not supported");
    assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me/avatar");
    assertThat(response.getBody().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("handleMethodArgumentTypeMismatchException should return 400 Bad Request with proper ErrorResponse")
  void handleMethodArgumentTypeMismatchException_ShouldReturn400() {
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex =
        new org.springframework.web.method.annotation.MethodArgumentTypeMismatchException(
            "not-and-uuid",
            java.util.UUID.class,
            "id",
            null,
            new IllegalArgumentException("Invalid UUID string: not-and-uuid"));

    ResponseEntity<ErrorResponse> response =
        handler.handleMethodArgumentTypeMismatchException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo(ErrorCode.BAD_REQUEST.getError());
    assertThat(response.getBody().getMessage()).contains("Failed to convert value of type");
    assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me/avatar");
    assertThat(response.getBody().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("handleAuthorizationDeniedException should return 403 Forbidden")
  void handleAuthorizationDeniedException_ShouldReturn403() {
    ResponseEntity<ErrorResponse> response = handler.handleAuthorizationDeniedException(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getError()).isEqualTo("Forbidden");
    assertThat(response.getBody().getMessage())
        .isEqualTo("You do not have the required permissions to perform this action");
  }
}
