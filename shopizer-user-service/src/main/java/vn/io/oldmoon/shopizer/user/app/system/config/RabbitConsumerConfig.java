package vn.io.oldmoon.shopizer.user.app.system.config;

@Configuration
public class RabbitConsumerConfig {

  @Bean
  public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      DefaultMessageHandlerMethodFactory messageHandlerMethodFactory) {

    DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    // Important: this allows the listener to use the JSON converter
    return factory;
  }

  @Bean
  public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
    DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
    // Uses the same Jackson converter we used for publishing
    factory.setMessageConverter(new MappingJackson2MessageConverter());
    return factory;
  }
}
