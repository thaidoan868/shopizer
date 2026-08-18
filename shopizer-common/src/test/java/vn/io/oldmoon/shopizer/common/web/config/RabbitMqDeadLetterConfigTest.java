package vn.io.oldmoon.shopizer.common.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.test.util.ReflectionTestUtils;

class RabbitMqDeadLetterConfigTest {

  private RabbitMqDeadLetterConfig config;
  private ConnectionFactory connectionFactory;

  @BeforeEach
  void setUp() {
    config = new RabbitMqDeadLetterConfig();
    connectionFactory = mock(ConnectionFactory.class);
  }

  @Test
  @DisplayName("Should create RejectAndDontRequeueRecoverer bean")
  void shouldCreateRejectAndDontRequeueRecoverer() {
    RejectAndDontRequeueRecoverer recoverer = config.rejectAndDontRequeueRecoverer();

    assertThat(recoverer).isNotNull();
  }

  @Test
  @DisplayName("Should create RetryOperationsInterceptor with configured retry policy")
  void shouldCreateRetryInterceptor() {
    RejectAndDontRequeueRecoverer recoverer = config.rejectAndDontRequeueRecoverer();
    ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();

    RetryOperationsInterceptor interceptor = config.retryInterceptor(recoverer, retryPolicy);

    assertThat(interceptor).isNotNull();
  }

  @Test
  @DisplayName("Should configure SimpleRabbitListenerContainerFactory with expected properties")
  void shouldConfigureRabbitListenerContainerFactory() {
    RetryOperationsInterceptor retryInterceptor = mock(RetryOperationsInterceptor.class);

    SimpleRabbitListenerContainerFactory factory =
        config.rabbitListenerContainerFactory(connectionFactory, retryInterceptor);

    assertThat(factory).isNotNull();

    Object messageConverter = ReflectionTestUtils.getField(factory, "messageConverter");
    assertThat(messageConverter).isInstanceOf(Jackson2JsonMessageConverter.class);
  }

  @Test
  @DisplayName("Should create RabbitTemplate configured with Jackson2JsonMessageConverter")
  void shouldCreateRabbitTemplate() {
    RabbitTemplate template = config.rabbitTemplate(connectionFactory);

    assertThat(template).isNotNull();
    assertThat(template.getConnectionFactory()).isEqualTo(connectionFactory);
    assertThat(template.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
  }
}
