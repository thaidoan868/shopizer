package vn.io.oldmoon.shopizer.common.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTransientConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import vn.io.oldmoon.shopizer.common.core.exception.FatalException;

class RabbitMqRetryPolicyTest {

  private RabbitMqRetryPolicy config;
  private SimpleRetryPolicy simpleRetryPolicy;
  private NeverRetryPolicy neverRetryPolicy;
  private ConditionalRejectingErrorHandler.DefaultExceptionStrategy defaultStrategy;
  private ExceptionClassifierRetryPolicy classifierPolicy;

  @BeforeEach
  void setUp() {
    config = new RabbitMqRetryPolicy();
    simpleRetryPolicy = config.simpleRetryPolicy();
    neverRetryPolicy = config.neverRetryPolicy();
    defaultStrategy = config.defaultExceptionStrategy();
    classifierPolicy =
        config.exceptionClassifierRetryPolicy(neverRetryPolicy, defaultStrategy, simpleRetryPolicy);
  }

  @Test
  @DisplayName("NeverRetryPolicy should always return false after throwable")
  void neverRetryPolicyShouldNeverRetry() {
    RetryContext context = neverRetryPolicy.open(null);
    neverRetryPolicy.registerThrowable(context, new RuntimeException("Any error"));

    assertThat(neverRetryPolicy.canRetry(context)).isFalse();
  }

  // --- Helper method ---
  private boolean canRetry(SimpleRetryPolicy policy, Throwable throwable) {
    RetryContext context = policy.open(null);
    policy.registerThrowable(context, throwable);
    return policy.canRetry(context);
  }

  @Nested
  @DisplayName("SimpleRetryPolicy Tests")
  class SimpleRetryPolicyTests {

    @Test
    @DisplayName("Should allow retry for transient network and server exceptions")
    void shouldRetryTransientExceptions() {
      assertThat(canRetry(simpleRetryPolicy, new SocketTimeoutException("timeout"))).isTrue();
      assertThat(canRetry(simpleRetryPolicy, new ConnectException("refused"))).isTrue();
      assertThat(canRetry(simpleRetryPolicy, mock(HttpServerErrorException.class))).isTrue();
      assertThat(canRetry(simpleRetryPolicy, mock(FeignException.InternalServerError.class)))
          .isTrue();
      assertThat(canRetry(simpleRetryPolicy, mock(FeignException.ServiceUnavailable.class)))
          .isTrue();
    }

    @Test
    @DisplayName("Should allow retry for transient database exceptions")
    void shouldRetryTransientDbExceptions() {
      assertThat(canRetry(simpleRetryPolicy, new SQLTransientConnectionException())).isTrue();
      assertThat(canRetry(simpleRetryPolicy, new CannotAcquireLockException("lock"))).isTrue();
      assertThat(canRetry(simpleRetryPolicy, new QueryTimeoutException("timeout"))).isTrue();
    }

    @Test
    @DisplayName("Should NOT allow retry for business logic & data validation exceptions")
    void shouldNotRetryBusinessLogicExceptions() {
      assertThat(canRetry(simpleRetryPolicy, new IllegalArgumentException())).isFalse();
      assertThat(canRetry(simpleRetryPolicy, new NullPointerException())).isFalse();
      assertThat(canRetry(simpleRetryPolicy, new IllegalStateException())).isFalse();
      assertThat(canRetry(simpleRetryPolicy, new IndexOutOfBoundsException())).isFalse();
      assertThat(canRetry(simpleRetryPolicy, new ClassCastException())).isFalse();
      assertThat(canRetry(simpleRetryPolicy, new ArithmeticException())).isFalse();
      assertThat(canRetry(simpleRetryPolicy, new SQLIntegrityConstraintViolationException()))
          .isFalse();
    }

    @Test
    @DisplayName("Should NOT allow retry for HTTP 4xx client errors")
    void shouldNotRetry4xxErrors() {
      assertThat(canRetry(simpleRetryPolicy, mock(HttpClientErrorException.class))).isFalse();
      assertThat(canRetry(simpleRetryPolicy, mock(FeignException.BadRequest.class))).isFalse();
      assertThat(canRetry(simpleRetryPolicy, mock(FeignException.NotFound.class))).isFalse();
      assertThat(canRetry(simpleRetryPolicy, mock(FeignException.UnprocessableEntity.class)))
          .isFalse();
    }

    @Test
    @DisplayName("Should NOT allow retry for custom FatalException")
    void shouldNotRetryFatalException() {
      assertThat(canRetry(simpleRetryPolicy, new FatalException("Fatal Error"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Should respect max retry attempts count")
    void shouldRespectMaxAttempts(int attemptNumber) {
      RetryContext context = simpleRetryPolicy.open(null);

      for (int i = 0; i < attemptNumber; i++) {
        simpleRetryPolicy.registerThrowable(context, new SocketTimeoutException());
      }

      if (attemptNumber < 3) {
        assertThat(simpleRetryPolicy.canRetry(context)).isTrue();
      } else {
        assertThat(simpleRetryPolicy.canRetry(context)).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("ExceptionClassifierRetryPolicy Tests")
  class ExceptionClassifierRetryPolicyTests {

    @Test
    @DisplayName("Should classify standard retryable exception to SimpleRetryPolicy")
    void shouldClassifyToSimpleRetryPolicy() {
      RetryContext context = classifierPolicy.open(null);
      classifierPolicy.registerThrowable(context, new SocketTimeoutException("timeout"));

      // SimpleRetryPolicy will allow retry on first attempt for transient error
      assertThat(classifierPolicy.canRetry(context)).isTrue();
    }

    @Test
    @DisplayName(
        "Should classify fatal exception (via DefaultExceptionStrategy) to NeverRetryPolicy")
    void shouldClassifyToNeverRetryPolicyForFatalExceptions() {
      // Mock DefaultExceptionStrategy to mark a specific exception as fatal
      ConditionalRejectingErrorHandler.DefaultExceptionStrategy mockStrategy =
          mock(ConditionalRejectingErrorHandler.DefaultExceptionStrategy.class);

      ExceptionClassifierRetryPolicy customClassifierPolicy =
          config.exceptionClassifierRetryPolicy(neverRetryPolicy, mockStrategy, simpleRetryPolicy);

      RuntimeException fatalException = new RuntimeException("Fatal error");
      when(mockStrategy.isFatal(fatalException)).thenReturn(true);

      RetryContext context = customClassifierPolicy.open(null);
      customClassifierPolicy.registerThrowable(context, fatalException);

      // NeverRetryPolicy will return false immediately
      assertThat(customClassifierPolicy.canRetry(context)).isFalse();
    }
  }
}
