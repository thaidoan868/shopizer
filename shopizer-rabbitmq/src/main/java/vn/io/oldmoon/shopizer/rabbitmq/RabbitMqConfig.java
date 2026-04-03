package vn.io.oldmoon.shopizer.rabbitmq;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

  // QUEUES
  @Bean
  public TopicExchange mainExchange() {
    return new TopicExchange(RabbitConstants.MAIN_EXCHANGE);
  }

  @Bean
  public Queue userCreatedQueue() {
    return QueueBuilder.durable(RabbitConstants.USER_CREATED_QUEUE).build();
  }

  @Bean
  public Binding userCreatedBinding() {
    return BindingBuilder.bind(userCreatedQueue())
        .to(mainExchange())
        .with(RabbitConstants.USER_CREATED_KEY);
  }

  // Dead Letter Infrastructure
  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(RabbitConstants.DLX_EXCHANGE);
  }

  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder.durable(RabbitConstants.DLQ).build();
  }

  @Bean
  public Binding dlqBinding() {
    return BindingBuilder.bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with(RabbitConstants.DEAD_LETTER_ROUTING_KEY);
  }

  // retry

  @Bean
  public RepublishMessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate) {
    return new RepublishMessageRecoverer(
        rabbitTemplate, RabbitConstants.DLX_EXCHANGE, RabbitConstants.DEAD_LETTER_ROUTING_KEY);
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
