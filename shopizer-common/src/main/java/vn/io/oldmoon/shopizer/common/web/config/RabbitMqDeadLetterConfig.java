package vn.io.oldmoon.shopizer.common.web.config;

import java.util.UUID;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.MDC;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import vn.io.oldmoon.shopizer.common.web.component.MdcCorrelationIdFilter;
import vn.io.oldmoon.shopizer.common.web.model.Header;

@Configuration
@EnableRabbit
public class RabbitMqDeadLetterConfig {
  /**
   * Rejects the message without requeueing when retries are exhausted. RabbitMQ will natively
   * transfer it to the queue's x-dead-letter-exchange and x-dead-letter-routing-key.
   */
  @Bean
  public RejectAndDontRequeueRecoverer rejectAndDontRequeueRecoverer() {
    return new RejectAndDontRequeueRecoverer();
  }

  @Bean
  public RetryOperationsInterceptor retryInterceptor(
      RejectAndDontRequeueRecoverer recoverer, ExceptionClassifierRetryPolicy retryPolicy) {

    return RetryInterceptorBuilder.stateless()
        .retryPolicy(retryPolicy)
        .backOffOptions(1000, 2.0, 10000) // initial, multiplier, max
        .recoverer(recoverer)
        .build();
  }

  /**
   * Advice that extracts the correlation ID and message ID from the incoming RabbitMQ message (or
   * generates a new UUID if missing) and binds them to SLF4J MDC for the duration of message
   * processing.
   */
  @Bean
  public MethodInterceptor rabbitMdcCorrelationIdInterceptor() {
    return invocation -> {
      String correlationId = extractCorrelationId(invocation.getArguments());
      String messageId = extractMessageId(invocation.getArguments());
      MDC.put(MdcCorrelationIdFilter.MDC_KEY, correlationId);
      MDC.put(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY, messageId);
      try {
        return invocation.proceed();
      } finally {
        MDC.remove(MdcCorrelationIdFilter.MDC_KEY);
        MDC.remove(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY);
      }
    };
  }

  private String extractCorrelationId(Object[] args) {
    if (args != null) {
      for (Object arg : args) {
        if (arg instanceof Message message) {
          MessageProperties props = message.getMessageProperties();
          if (props != null) {
            Object headerVal = props.getHeader(Header.CORRELATION_ID);
            if (headerVal == null) {
              headerVal = props.getHeader(MdcCorrelationIdFilter.MDC_KEY);
            }
            if (headerVal != null && !headerVal.toString().isBlank()) {
              return headerVal.toString();
            }
            if (props.getCorrelationId() != null && !props.getCorrelationId().isBlank()) {
              return props.getCorrelationId();
            }
          }
        }
      }
    }
    return UUID.randomUUID().toString();
  }

  private String extractMessageId(Object[] args) {
    if (args != null) {
      for (Object arg : args) {
        if (arg instanceof Message message) {
          MessageProperties props = message.getMessageProperties();
          if (props != null) {
            Object headerVal = props.getHeader(Header.MESSAGE_ID);
            if (headerVal == null) {
              headerVal = props.getHeader("x-message-id");
            }
            if (headerVal == null) {
              headerVal = props.getHeader(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY);
            }
            if (headerVal != null && !headerVal.toString().isBlank()) {
              return headerVal.toString();
            }
            if (props.getMessageId() != null && !props.getMessageId().isBlank()) {
              return props.getMessageId();
            }
          }
        }
      }
    }
    return UUID.randomUUID().toString();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      RetryOperationsInterceptor retryInterceptor,
      @Qualifier("rabbitMdcCorrelationIdInterceptor")
          MethodInterceptor rabbitMdcCorrelationIdInterceptor) {

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(new Jackson2JsonMessageConverter());

    factory.setConcurrentConsumers(3);
    factory.setMaxConcurrentConsumers(10);
    factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

    // This is the "brain" that decides what to do with failed messages
    Advice[] adviceChain = {rabbitMdcCorrelationIdInterceptor, retryInterceptor};
    factory.setAdviceChain(adviceChain);

    return factory;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(new Jackson2JsonMessageConverter());
    template.addBeforePublishPostProcessors(
        message -> {
          String correlationId = MDC.get(MdcCorrelationIdFilter.MDC_KEY);
          if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
          }
          MessageProperties props = message.getMessageProperties();
          if (props != null) {
            if (props.getHeader(Header.CORRELATION_ID) == null) {
              props.setHeader(Header.CORRELATION_ID, correlationId);
            }
            if (props.getCorrelationId() == null || props.getCorrelationId().isBlank()) {
              props.setCorrelationId(correlationId);
            }
            String messageId = UUID.randomUUID().toString();
            if (props.getHeader(Header.MESSAGE_ID) == null) {
              props.setHeader(Header.MESSAGE_ID, messageId);
            }
            if (props.getMessageId() == null || props.getMessageId().isBlank()) {
              props.setMessageId(messageId);
            }
          }
          return message;
        });
    return template;
  }
}
