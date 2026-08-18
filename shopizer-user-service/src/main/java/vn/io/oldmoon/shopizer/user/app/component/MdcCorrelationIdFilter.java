package vn.io.oldmoon.shopizer.user.app.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Header;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcCorrelationIdFilter extends OncePerRequestFilter {

  public static final String MDC_KEY = "correlationId";

  /**
   * Forward the correlation id header or create a new one if missing. Add correlationId to
   * MDC(logger)
   */
  @Override
  @Order(Ordered.HIGHEST_PRECEDENCE)
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = request.getHeader(Header.CORRELATION_ID);

    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    response.setHeader(Header.CORRELATION_ID, correlationId);

    MDC.put(MDC_KEY, correlationId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
