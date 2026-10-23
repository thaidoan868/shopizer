package vn.io.oldmoon.shopizer.common.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.oldmoon.shopizer.common.web.component.MdcCorrelationIdFilter;
import vn.io.oldmoon.shopizer.common.web.model.Header;

class RabbitMqDeadLetterConfigTest {

  private RabbitMqDeadLetterConfig config;
  private ConnectionFactory connectionFactory;

  @BeforeEach
  void setUp() {
    config = new RabbitMqDeadLetterConfig();
    connectionFactory = mock(ConnectionFactory.class);
    MDC.clear();
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
    MethodInterceptor mdcInterceptor = config.rabbitMdcCorrelationIdInterceptor();

    SimpleRabbitListenerContainerFactory factory =
        config.rabbitListenerContainerFactory(connectionFactory, retryInterceptor, mdcInterceptor);

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

  @Test
  @DisplayName("Should extract correlationId and messageId from message headers and clear MDC after invocation")
  void shouldExtractCorrelationIdAndMessageIdFromHeaderAndClearMdc() throws Throwable {
    MethodInterceptor interceptor = config.rabbitMdcCorrelationIdInterceptor();
    MethodInvocation invocation = mock(MethodInvocation.class);

    MessageProperties props = new MessageProperties();
    props.setHeader(Header.CORRELATION_ID, "custom-corr-id-123");
    props.setHeader(Header.MESSAGE_ID, "custom-msg-id-789");
    Message message = new Message(new byte[0], props);

    when(invocation.getArguments()).thenReturn(new Object[] {null, message});
    when(invocation.proceed())
        .thenAnswer(
            inv -> {
              assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isEqualTo("custom-corr-id-123");
              assertThat(MDC.get(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY))
                  .isEqualTo("custom-msg-id-789");
              return "result";
            });

    Object result = interceptor.invoke(invocation);

    assertThat(result).isEqualTo("result");
    assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isNull();
    assertThat(MDC.get(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Should extract correlationId and messageId from message properties when headers missing")
  void shouldExtractCorrelationIdAndMessageIdFromMessagePropertiesWhenHeaderMissing()
      throws Throwable {
    MethodInterceptor interceptor = config.rabbitMdcCorrelationIdInterceptor();
    MethodInvocation invocation = mock(MethodInvocation.class);

    MessageProperties props = new MessageProperties();
    props.setCorrelationId("amqp-corr-id-456");
    props.setMessageId("amqp-msg-id-999");
    Message message = new Message(new byte[0], props);

    when(invocation.getArguments()).thenReturn(new Object[] {null, message});
    when(invocation.proceed())
        .thenAnswer(
            inv -> {
              assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isEqualTo("amqp-corr-id-456");
              assertThat(MDC.get(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY))
                  .isEqualTo("amqp-msg-id-999");
              return "result";
            });

    interceptor.invoke(invocation);

    assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isNull();
    assertThat(MDC.get(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY)).isNull();
  }

  @Test
  @DisplayName("Should generate UUID correlationId and messageId when missing from message and clear MDC on exception")
  void shouldGenerateUuidWhenMissingAndClearMdcOnException() throws Throwable {
    MethodInterceptor interceptor = config.rabbitMdcCorrelationIdInterceptor();
    MethodInvocation invocation = mock(MethodInvocation.class);

    Message message = new Message(new byte[0], new MessageProperties());
    when(invocation.getArguments()).thenReturn(new Object[] {null, message});
    when(invocation.proceed())
        .thenAnswer(
            inv -> {
              String mdcVal = MDC.get(MdcCorrelationIdFilter.MDC_KEY);
              String msgIdVal = MDC.get(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY);
              assertThat(mdcVal).isNotBlank();
              assertThat(UUID.fromString(mdcVal)).isNotNull();
              assertThat(msgIdVal).isNotBlank();
              assertThat(UUID.fromString(msgIdVal)).isNotNull();
              throw new RuntimeException("Listener error");
            });

    assertThatThrownBy(() -> interceptor.invoke(invocation))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Listener error");

    assertThat(MDC.get(MdcCorrelationIdFilter.MDC_KEY)).isNull();
    assertThat(MDC.get(MdcCorrelationIdFilter.MESSAGE_ID_MDC_KEY)).isNull();
  }
}
