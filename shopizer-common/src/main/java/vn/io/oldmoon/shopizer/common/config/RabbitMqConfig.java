package vn.io.oldmoon.shopizer.common.config;

@Configuration
public class RabbitMqConfig {

  // A QUEUE
  public static final String ORDER_QUEUE = "order.queue";
  public static final String ORDER_DLQ = "order.queue.dlq";
  public static final String DLX = "order.dlx";

  // The RabbitTemplate is the core client that handles sending messages
  @Bean
  @ConditionalOnMissingBean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    // This ensures the message is converted to JSON automatically
    template.setMessageConverter(jsonMessageConverter());
    return template;
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
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

  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DLX);
  }

  @Bean
  public Queue orderQueue() {
    return QueueBuilder.durable(ORDER_QUEUE)
        .deadLetterExchange(DLX)
        .deadLetterRoutingKey(ORDER_DLQ)
        .build();
  }

  @Bean
  public Queue orderDlq() {
    return QueueBuilder.durable(ORDER_DLQ).build();
  }

  @Bean
  public Binding dlqBinding() {
    return BindingBuilder
        .bind(orderDlq())
        .to(deadLetterExchange())
        .with(ORDER_DLQ);
  }
}
