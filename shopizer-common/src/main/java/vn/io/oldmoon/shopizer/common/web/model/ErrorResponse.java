package vn.io.oldmoon.shopizer.common.web.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ErrorResponse {
  private final String error;
  private final String message;
  private final List<String> errors;
  private final String path;
  private LocalDateTime timestamp = LocalDateTime.now();

  public ErrorResponse(String error, String message, String path) {
    this.error = error;
    this.message = message;
    this.path = path;
    this.errors = null;
    this.timestamp = LocalDateTime.now();
  }
}
