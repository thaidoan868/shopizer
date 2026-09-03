package vn.io.oldmoon.shopizer.user.business.event.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.service.profile.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@SpringBootTest
@Testcontainers
class KeycloakUserRegisteredEventListenerIT {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  // prevent Keycloak from connecting to the failed keycloak container
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;
  @MockitoSpyBean private CustomerCreatedEventListener customerCreatedEventListener;
  @Autowired private RabbitTemplate rabbitTemplate;

  @Autowired private CustomerProfileRepository customerProfileRepository;

  @MockitoSpyBean private CustomerProfileService customerProfileService;
  private KeycloakUserRegisteredEvent event;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // Disable requeue on error so failed messages get routed directly to the DLX
    registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");
  }

  @BeforeEach
  void setUp() {
    UUID keycloakUserId = UUID.randomUUID();
    KeycloakRegistrationDetails details =
        new KeycloakRegistrationDetails(
            "openid-connect",
            "code", // auth_type
            "form", // register_method
            "Doe", // last_name
            "http://localhost/callback",
            "John", // first_name
            UUID.randomUUID().toString(),
            UUID.randomUUID() + "@example.com",
            "johndoe" + UUID.randomUUID());
    this.event = new KeycloakUserRegisteredEvent(keycloakUserId, details);
  }

  @Test
  @DisplayName("Happy Path: Accept event, create CustomerProfile, and publish CustomerCreatedEvent")
  void handle_HappyPath_ShouldCreateProfileAndPublishDownstreamEvent() {
    // Given
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.userRegisteredBindingKey, event);

    // Then 1: Verify CustomerProfile was persisted in DB
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<CustomerProfileQueryDto> profile =
                  customerProfileRepository.findByKeycloakUserId(event.userId());
              assertThat(profile).isPresent();
            });

    // Verify that your downstream listener bean actually handled the event
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                verify(customerCreatedEventListener)
                    .handle(
                        argThat(
                            receivedEvent ->
                                receivedEvent.keycloakUserId().equals(event.userId().toString()))));
  }

  @Test
  @DisplayName(
      "Bad Path: When profile creation fails, message should be routed to Dead Letter Queue")
  void handle_WhenProfileCreationFailed_ShouldRouteToDeadLetterQueue() {
    // Force customerProfileService.create(...) to throw a runtime exception
    doThrow(new RuntimeException("Database error during profile creation"))
        .when(customerProfileService)
        .create(any(User.class));

    // When: Publish to userEventExchange
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.userRegisteredBindingKey, event);

    // Then: Verify message landed in userRegisteredDlq
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Message dlqMessage = rabbitTemplate.receive(RabbitMqConfig.userRegisteredDlq);
              assertThat(dlqMessage).isNotNull();

              // Verify the dead-lettered message body retains original payload metadata
              String body = new String(dlqMessage.getBody());
              assertThat(body).contains(event.userId().toString());
            });
  }

  @Test
  @DisplayName(
      "Bad path: Resilience & Idempotency: Duplicate events with same keycloak user ID should not cause primary key violation")
  void handle_Idempotency_DuplicateEvents_ShouldNotCausePrimaryKeyViolation() {
    // When: Send first event
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.userRegisteredBindingKey, event);

    // Verify first event persisted
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<CustomerProfileQueryDto> customerProfileQueryDto =
                  customerProfileRepository.findByKeycloakUserId(event.userId());
              assertThat(customerProfileQueryDto).isPresent();
            });

    // When: Send duplicate event with same user ID
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.userRegisteredBindingKey, event);

    // Verify the listener method was invoked 2 times total without throwing an exception
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> verify(customerCreatedEventListener, times(2)).handle(any()));
  }
}
