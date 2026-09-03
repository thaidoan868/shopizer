package vn.io.oldmoon.shopizer.common.core.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
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
}
