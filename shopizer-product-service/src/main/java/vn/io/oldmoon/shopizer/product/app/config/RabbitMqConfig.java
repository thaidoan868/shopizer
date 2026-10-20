package vn.io.oldmoon.shopizer.product.app.config;

import java.util.concurrent.Executor;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

  public static final String productEventExchange = "product.events";
  public static final String deadLetterExchange = "product.events.dlx";

  public static final String videoProcessingQueue = "video-processing-queue";
  public static final String videoProcessingBindingKey = "product.media.video.process";
  public static final String videoProcessingDlq = "video-processing-dlq";
  public static final String videoProcessingDlqBindingKey = "product.media.video.process.dlq";

  private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
  private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

  @Bean
  public TopicExchange productEventExchange() {
    return new TopicExchange(productEventExchange);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(deadLetterExchange);
  }

  @Bean
  public Queue videoProcessingQueue() {
    return QueueBuilder.durable(videoProcessingQueue)
        .withArgument(X_DEAD_LETTER_EXCHANGE, deadLetterExchange)
        .withArgument(X_DEAD_LETTER_ROUTING_KEY, videoProcessingDlqBindingKey)
        .build();
  }

  @Bean
  public Binding videoProcessingBinding() {
    return BindingBuilder.bind(videoProcessingQueue())
        .to(productEventExchange())
        .with(videoProcessingBindingKey);
  }

  @Bean
  public Queue videoProcessingDlq() {
    return QueueBuilder.durable(videoProcessingDlq).build();
  }

  @Bean
  public Binding videoProcessingDlqBinding() {
    return BindingBuilder.bind(videoProcessingDlq())
        .to(deadLetterExchange())
        .with(videoProcessingDlqBindingKey);
  }

  /**
   * Dedicated thread pool for concurrent FFmpeg video transcoding and queue processing.
   */
  @Bean(name = "videoProcessingExecutor")
  public Executor videoProcessingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("video-ffmpeg-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "videoRabbitListenerContainerFactory")
  public SimpleRabbitListenerContainerFactory videoRabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      RetryOperationsInterceptor retryInterceptor,
      @Qualifier("videoProcessingExecutor") Executor videoProcessingExecutor) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(new Jackson2JsonMessageConverter());
    factory.setTaskExecutor(videoProcessingExecutor);
    factory.setConcurrentConsumers(2);
    factory.setMaxConcurrentConsumers(4);
    factory.setPrefetchCount(1);
    factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
    factory.setAdviceChain(new Advice[] {retryInterceptor});
    return factory;
  }
}
