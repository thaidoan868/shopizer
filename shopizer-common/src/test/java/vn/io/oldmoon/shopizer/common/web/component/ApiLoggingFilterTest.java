package vn.io.oldmoon.shopizer.common.web.component;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiLoggingFilterTest {

  private final ApiLoggingFilter filter = new ApiLoggingFilter();

  @Test
  void shouldPassRequestDownFilterChain() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldExecuteFinallyBlockWhenDownstreamFails() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/orders");
    HttpServletResponse response = mock(HttpServletResponse.class); // Mockito mock
    FilterChain filterChain = mock(FilterChain.class);

    doThrow(new RuntimeException("Database unreachable"))
        .when(filterChain)
        .doFilter(request, response);

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Database unreachable");
    verify(response).getStatus();
  }
}
