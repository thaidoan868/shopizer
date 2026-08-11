package vn.io.oldmoon.shopizer.user.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.*;

@Configuration
@EnableRabbit
public class RabbitMqConfig {
  public static final String userEventExchange = "user.events";
  public static final String userRegisteredQueue = "user-registered-queue";
  public static final String userRegisteredBindingKey =
      "KK.EVENT.CLIENT.shopizer.SUCCESS.account-console.REGISTER";

  @Bean
  public TopicExchange userEventExchange() {
    return new TopicExchange(userEventExchange);
  }

  @Bean
  public Queue userRegisteredQueue() {
    return QueueBuilder.durable(userRegisteredQueue).build();
  }

  @Bean
  public Binding userRegisteredBindingKey() {
    return BindingBuilder.bind(userRegisteredQueue())
        .to(userEventExchange())
        .with(userRegisteredBindingKey);
  }
}
