package vn.io.oldmoon.shopizer.user.app.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Header;

class MdcCorrelationIdFilterTest {

  private MdcCorrelationIdFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    filter = new MdcCorrelationIdFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = mock(FilterChain.class);
  }

  @Test
  void shouldReuseExistingCorrelationIdWhenHeaderIsPresent() throws Exception {
    String existingCorrelationId = "test-correlation-id-123";
    request.addHeader(Header.CORRELATION_ID, existingCorrelationId);

    // Verify MDC contains the ID DURING chain execution, and is cleared AFTER execution
    doAnswer(invocation -> {
      assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isEqualTo(existingCorrelationId);
      return null;
    }).when(filterChain).doFilter(request, response);

    filter.doFilter(request, response, filterChain);

    // Verify response header set and chain execution executed
    assertThat(response.getHeader(Header.CORRELATION_ID)).isEqualTo(existingCorrelationId);
    assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void shouldGenerateUuidWhenHeaderIsMissingOrBlank(String headerValue) throws Exception {
    if (headerValue != null) {
      request.addHeader(Header.CORRELATION_ID, headerValue);
    }

    doAnswer(invocation -> {
      String currentMdcId = MDC.get(MdcCorrelationIdFilter.MDC_KEY);
      assertThat(currentMdcId).isNotNull();
      assertThat(UUID.fromString(currentMdcId)).isNotNull(); // Validates standard UUID format
      return null;
    }).when(filterChain).doFilter(request, response);

    filter.doFilter(request, response, filterChain);

    String generatedId = response.getHeader(Header.CORRELATION_ID);
    assertThat(generatedId).isNotNull();
    assertThat(UUID.fromString(generatedId)).isNotNull();
    assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isNull();
  }

  @Test
  void shouldRemoveMdcKeyEvenWhenDownstreamFails() throws Exception {
    request.addHeader(Header.CORRELATION_ID, "id-before-failure");

    doThrow(new RuntimeException("Downstream processing failed"))
        .when(filterChain).doFilter(request, response);

    assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Downstream processing failed");

    // Proves finally block successfully cleaned up MDC context
    assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isNull();
  }
}