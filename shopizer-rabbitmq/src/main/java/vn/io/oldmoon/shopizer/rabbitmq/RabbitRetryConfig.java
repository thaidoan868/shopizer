package vn.io.oldmoon.shopizer.rabbitmq;

import feign.FeignException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTransientConnectionException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import vn.io.oldmoon.shopizer.common.core.exception.FatalException;

@Configuration
public class RabbitRetryConfig {
  @Bean
  public RetryOperationsInterceptor retryInterceptor(RepublishMessageRecoverer recoverer) {
    Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
    // --- BUSINESS LOGIC & DATA (Usually FATAL - DO NOT RETRY) ---
    retryableExceptions.put(IllegalArgumentException.class, false); // Bad method input
    retryableExceptions.put(NullPointerException.class, false); // Code bug
    retryableExceptions.put(IllegalStateException.class, false); // Wrong app state
    retryableExceptions.put(IndexOutOfBoundsException.class, false); // Data issue
    retryableExceptions.put(ClassCastException.class, false); // Logic error
    retryableExceptions.put(TypeMismatchException.class, false); // Unexpected type
    retryableExceptions.put(AccessDeniedException.class, false); // Permission denied
    retryableExceptions.put(
        SQLIntegrityConstraintViolationException.class, false); // DB constraint (unique, FK)
    retryableExceptions.put(
        MethodArgumentNotValidException.class, false); // Validation failed (Spring)
    retryableExceptions.put(
        HttpMessageNotReadableException.class, false); // Bad JSON / malformed request
    retryableExceptions.put(ArithmeticException.class, false); // Divide by zero etc.

    // --- CONFIGURATION ERRORS (FATAL) ---
    retryableExceptions.put(MalformedURLException.class, false); // Bad config
    retryableExceptions.put(
        UnknownHostException.class, false); // DNS/config issue (usually fatal in stable env)

    // --- EXTERNAL INTEGRATIONS (DEPENDS, BUT MOSTLY FATAL) ---
    retryableExceptions.put(UnsupportedOperationException.class, false); // Not implemented
    retryableExceptions.put(
        HttpClientErrorException.class, false); // 4xx errors (bad request, unauthorized)
    retryableExceptions.put(FeignException.BadRequest.class, false); // 400
    retryableExceptions.put(FeignException.NotFound.class, false); // 404
    retryableExceptions.put(FeignException.UnprocessableEntity.class, false); // 422

    // --- TRANSIENT ERRORS (RETRYABLE - IMPORTANT ADDITION) ---
    retryableExceptions.put(SocketTimeoutException.class, true); // Network timeout
    retryableExceptions.put(ConnectException.class, true); // Connection refused
    retryableExceptions.put(HttpServerErrorException.class, true); // 5xx server errors
    retryableExceptions.put(FeignException.InternalServerError.class, true); // 500
    retryableExceptions.put(FeignException.ServiceUnavailable.class, true); // 503
    retryableExceptions.put(SQLTransientConnectionException.class, true); // DB connection issue
    retryableExceptions.put(CannotAcquireLockException.class, true); // DB lock contention
    retryableExceptions.put(QueryTimeoutException.class, true); // DB timeout

    retryableExceptions.put(FatalException.class, false);
    SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true, true);

    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(1000);
    backOffPolicy.setMultiplier(2.0);
    backOffPolicy.setMaxInterval(10000);

    return RetryInterceptorBuilder.stateless()
        .retryPolicy(simpleRetryPolicy)
        .backOffPolicy(backOffPolicy)
        .recoverer(recoverer)
        .build();
  }
}
