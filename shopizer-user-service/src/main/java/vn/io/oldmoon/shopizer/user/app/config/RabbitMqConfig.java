package vn.io.oldmoon.shopizer.user.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.*;

@Configuration
@EnableRabbit
public class RabbitMqConfig {
  public static final String userEventExchange = "user.events";
  public static final String userCreatedQueue = "user-created-queue";
  public static final String userCreatedBindingKey = "user.created";

  @Bean
  public TopicExchange userEventExchange() {
    return new TopicExchange(userEventExchange);
  }

  @Bean
  public Queue userCreatedQueue() {
    return QueueBuilder.durable(userCreatedQueue).build();
  }

  @Bean
  public Binding userCreatedBinding() {
    return BindingBuilder.bind(userCreatedQueue())
        .to(userEventExchange())
        .with(userCreatedBindingKey);
  }
}
