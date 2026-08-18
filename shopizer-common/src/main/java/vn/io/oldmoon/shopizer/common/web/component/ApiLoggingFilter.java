package vn.io.oldmoon.shopizer.common.web.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApiLoggingFilter extends OncePerRequestFilter {

  /** Log request and response summary information */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long startTime = System.currentTimeMillis();
    String path = request.getRequestURI();
    String method = request.getMethod();

    log.info("Request: {} {}", method, path);

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      int status = response.getStatus();
      log.info("Response: {} {} | Status: {} | Duration: {}ms", method, path, status, duration);
    }
  }
}
