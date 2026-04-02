package vn.io.oldmoon.shopizer.rabbitmq;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMqConfig {
  private static final String MAIN_EXCHANGE = "app.main.exchange";
  private static final String ORDER_CREATED = "order.created.key";

  private final String DLX_EXCHANGE = "app.dlx.exchange";
  private final String DLQ = "app.dead.letter.queue";
  private final String DEAD_LETTER_ROUTING_KEY = "deadLetter";

  // QUEUES
  @Bean
  public TopicExchange mainExchange() {
    return new TopicExchange(MAIN_EXCHANGE);
  }

  @Bean
  public Queue orderCreatedQueue() {
    return QueueBuilder.durable(ORDER_CREATED).build();
  }

  @Bean
  public Binding orderCreatedBinding() {
    return BindingBuilder.bind(orderCreatedQueue()).to(mainExchange()).with(ORDER_CREATED);
  }

  // Dead Letter Infrastructure
  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DLX_EXCHANGE);
  }

  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder.durable(DLQ).build();
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
    return new RepublishMessageRecoverer(rabbitTemplate, DLX_EXCHANGE, DEAD_LETTER_ROUTING_KEY);
  }

  @Bean
  public RetryOperationsInterceptor retryInterceptor(RepublishMessageRecoverer recoverer) {
    return RetryInterceptorBuilder.stateless()
        .maxAttempts(3)
        .backOffOptions(1000, 2.0, 10000) // 1s, 2s, 4s... up to 10s
        .recoverer(recoverer)
        .build();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory, RetryOperationsInterceptor retryInterceptor) {

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(new Jackson2JsonMessageConverter());

    // This is the "brain" that decides what to do with failed messages
    Advice[] adviceChain = {retryInterceptor};
    factory.setAdviceChain(adviceChain);

    return factory;
  }
}
