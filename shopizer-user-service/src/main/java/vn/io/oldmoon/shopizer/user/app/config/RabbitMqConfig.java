package vn.io.oldmoon.shopizer.user.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.*;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

  // Exchanges
  public static final String userEventExchange = "user.events";
  public static final String deadLetterExchange = "user.events.dlx";
  // 1. User Registered Queue & DLQ
  public static final String userRegisteredQueue = "user-registered-queue";
  public static final String userRegisteredBindingKey =
      "KK.EVENT.CLIENT.shopizer.SUCCESS.account-console.REGISTER";
  public static final String userRegisteredBySecurityConsoleBindingKey =
      "KK.EVENT.CLIENT.shopizer.SUCCESS.security-admin-console.REGISTER";
  public static final String userRegisteredDlq = "user-registered-dlq";
  public static final String userRegisteredDlqBindingKey = "user-registered.dlq";
  // 2. Customer Created Queue & DLQ
  public static final String customerCreatedQueue = "customer-created-queue";
  public static final String customerCreatedBindingKey = "user.customer.created";
  public static final String customerCreatedDlq = "customer-created-dlq";
  public static final String customerCreatedDlqBindingKey = "user.customer.created.dlq";
  // 3. User Created Queue & DLQ (Admin Event)
  public static final String AdminUserCreatedQueue = "user.events.admin.user-created";
  public static final String AdminUserCreatedBindingKey =
      "KK.EVENT.ADMIN.shopizer.SUCCESS.USER.CREATE";
  public static final String AdminUserCreatedDlq = "user.events.admin.user-created.dlq";
  public static final String AdminUserCreatedDlqBindingKey = "user.events.admin.user-created.dlq";

  private final String xDeadLetterExchange = "x-dead-letter-exchange";
  private final String xDeadLetterRoutingKey = "x-dead-letter-routing-key";

  // --- Exchanges Configuration ---
  @Bean
  public TopicExchange userEventExchange() {
    return new TopicExchange(userEventExchange);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(deadLetterExchange);
  }

  // --- 1. User Registered Configuration ---
  @Bean
  public Queue userRegisteredQueue() {
    return QueueBuilder.durable(userRegisteredQueue)
        .withArgument(xDeadLetterExchange, deadLetterExchange)
        .withArgument(xDeadLetterRoutingKey, userRegisteredDlqBindingKey)
        .build();
  }

  @Bean
  public Binding userRegisteredBinding() {
    return BindingBuilder.bind(userRegisteredQueue())
        .to(userEventExchange())
        .with(userRegisteredBindingKey);
  }

  @Bean
  public Binding userRegisteredBySecurityConsoleBinding() {
    return BindingBuilder.bind(userRegisteredQueue())
        .to(userEventExchange())
        .with(userRegisteredBySecurityConsoleBindingKey);
  }

  @Bean
  public Queue userRegisteredDlq() {
    return QueueBuilder.durable(userRegisteredDlq).build();
  }

  @Bean
  public Binding userRegisteredDlqBinding() {
    return BindingBuilder.bind(userRegisteredDlq())
        .to(deadLetterExchange())
        .with(userRegisteredDlqBindingKey);
  }

  // --- 2. Customer Created Configuration ---
  @Bean
  public Queue customerCreatedQueue() {
    return QueueBuilder.durable(customerCreatedQueue)
        .withArgument(xDeadLetterExchange, deadLetterExchange)
        .withArgument(xDeadLetterRoutingKey, customerCreatedDlqBindingKey)
        .build();
  }

  @Bean
  public Binding customerCreatedBinding() {
    return BindingBuilder.bind(customerCreatedQueue())
        .to(userEventExchange())
        .with(customerCreatedBindingKey);
  }

  @Bean
  public Queue customerCreatedDlq() {
    return QueueBuilder.durable(customerCreatedDlq).build();
  }

  @Bean
  public Binding customerCreatedDlqBinding() {
    return BindingBuilder.bind(customerCreatedDlq())
        .to(deadLetterExchange())
        .with(customerCreatedDlqBindingKey);
  }

  // --- 3. User Created (Admin Event) Configuration ---
  @Bean
  public Queue userCreatedQueue() {
    return QueueBuilder.durable(AdminUserCreatedQueue)
        .withArgument(xDeadLetterExchange, deadLetterExchange)
        .withArgument(xDeadLetterRoutingKey, AdminUserCreatedDlqBindingKey)
        .build();
  }

  @Bean
  public Binding userCreatedBinding() {
    return BindingBuilder.bind(userCreatedQueue())
        .to(userEventExchange())
        .with(AdminUserCreatedBindingKey);
  }

  @Bean
  public Queue userCreatedDlq() {
    return QueueBuilder.durable(AdminUserCreatedDlq).build();
  }

  @Bean
  public Binding userCreatedDlqBinding() {
    return BindingBuilder.bind(userCreatedDlq())
        .to(deadLetterExchange())
        .with(AdminUserCreatedDlqBindingKey);
  }
}
