package vn.io.oldmoon.shopizer.rabbitmq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
  // The RabbitTemplate is the core client that handles sending messages
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    // This ensures the message is converted to JSON automatically
    template.setMessageConverter(new Jackson2JsonMessageConverter());
    return template;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);

    // Tell Spring to use your brain + its default brain
    factory.setErrorHandler(
        new ConditionalRejectingErrorHandler(new MyBusinessFatalExceptionStrategy()));

    return factory;
  }

  //  @Bean
  //  public DirectExchange deadLetterExchange() {
  //    return new DirectExchange(DLX);
  //  }
  //
  //  @Bean
  //  public Queue orderQueue() {
  //    return QueueBuilder.durable(ORDER_QUEUE)
  //        .deadLetterExchange(DLX)
  //        .deadLetterRoutingKey(ORDER_DLQ)
  //        .build();
  //  }
  //
  //  @Bean
  //  public Queue orderDlq() {
  //    return QueueBuilder.durable(ORDER_DLQ).build();
  //  }
  //
  //  @Bean
  //  public Binding dlqBinding() {
  //    return BindingBuilder
  //        .bind(orderDlq())
  //        .to(deadLetterExchange())
  //        .with(ORDER_DLQ);
  //  }
}
