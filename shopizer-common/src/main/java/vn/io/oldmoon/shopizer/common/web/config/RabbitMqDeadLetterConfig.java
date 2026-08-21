package vn.io.oldmoon.shopizer.common.web.config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;

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

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory, RetryOperationsInterceptor retryInterceptor) {

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(new Jackson2JsonMessageConverter());

    factory.setConcurrentConsumers(3);
    factory.setMaxConcurrentConsumers(10);
    factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

    // This is the "brain" that decides what to do with failed messages
    Advice[] adviceChain = {retryInterceptor};
    factory.setAdviceChain(adviceChain);

    return factory;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(new Jackson2JsonMessageConverter());
    return template;
  }
}
