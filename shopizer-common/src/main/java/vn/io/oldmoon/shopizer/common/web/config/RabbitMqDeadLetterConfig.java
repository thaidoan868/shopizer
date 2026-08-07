package vn.io.oldmoon.shopizer.common.web.config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;

@Configuration
@EnableRabbit
public class RabbitMqDeadLetterConfig {
  private final String DEAD_LETTER_EXCHANGE = "app.deadletter.exchange";
  private final String DEAD_LETTER_QUEUE = "app.deadletter.queue";
  private final String DEAD_LETTER_ROUTING_KEY = "dead.event";

  // Dead Letter Infrastructure
  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DEAD_LETTER_EXCHANGE);
  }

  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
  }

  @Bean
  public Binding dlqBinding() {
    return BindingBuilder.bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with(DEAD_LETTER_ROUTING_KEY);
  }

  // retry

  @Bean
  public RepublishMessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate) {
    return new RepublishMessageRecoverer(
        rabbitTemplate, DEAD_LETTER_EXCHANGE, DEAD_LETTER_ROUTING_KEY);
  }

  @Bean
  public RetryOperationsInterceptor retryInterceptor(
      RepublishMessageRecoverer recoverer, ExceptionClassifierRetryPolicy retryPolicy) {

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
