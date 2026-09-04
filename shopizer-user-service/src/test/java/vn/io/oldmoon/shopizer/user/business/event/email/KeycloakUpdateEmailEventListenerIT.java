package vn.io.oldmoon.shopizer.user.business.event.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Mockito;
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
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@SpringBootTest
@Testcontainers
class KeycloakUpdateEmailEventListenerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private UserRepository userRepository;
  @MockitoSpyBean private UserService userService;

  private String initialEmail;
  private String newEmail;
  private String username;
  private UUID keycloakUserId;
  private User user;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }

  @BeforeEach
  void setUp() {
    this.keycloakUserId = UUID.randomUUID();
    this.username = "user-" + UUID.randomUUID().toString().substring(0, 8);
    this.initialEmail = "old-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
    this.newEmail = "new-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
    this.user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username(username)
            .email(initialEmail)
            .verified(true)
            .build();
    userRepository.save(user);
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(userService);
  }

  @Test
  @DisplayName("Happy Path: Receive UPDATE_PROFILE event, update user email and reset verified to false")
  void handle_HappyPath_ShouldUpdateUserEmailAndResetVerifiedToFalse() {
    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(newEmail)
            .previousEmail(initialEmail)
            .build();

    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .time(1788495128493L)
            .type("UPDATE_PROFILE")
            .realmId("shopizer")
            .clientId("account")
            .userId(keycloakUserId)
            .ipAddress("172.19.0.1")
            .details(details)
            .build();

    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account.UPDATE_PROFILE";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> updatedUser = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(updatedUser).isPresent();
              assertThat(updatedUser.get().getEmail()).isEqualTo(newEmail);
              assertThat(updatedUser.get().getVerified()).isFalse();
            });
  }

  @Test
  @DisplayName("Bad Path: When updated_email is null, message should be routed to Dead Letter Queue")
  void handle_WhenUpdatedEmailNull_ShouldRouteToDeadLetterQueue() {
    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(null)
            .previousEmail(initialEmail)
            .build();

    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .time(1788495128493L)
            .type("UPDATE_PROFILE")
            .realmId("shopizer")
            .clientId("account")
            .userId(keycloakUserId)
            .ipAddress("172.19.0.1")
            .details(details)
            .build();

    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account.UPDATE_PROFILE";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Message dlqMessage = rabbitTemplate.receive(RabbitMqConfig.updateEmailDlq);
              assertThat(dlqMessage).isNotNull();
              String body = new String(dlqMessage.getBody());
              assertThat(body).contains(keycloakUserId.toString());
            });
  }

  @Test
  @DisplayName("Bad Path: When user update fails, message should be routed to Dead Letter Queue")
  void handle_WhenProcessingFails_ShouldRouteToDeadLetterQueue() {
    doThrow(new RuntimeException("Simulated database failure"))
        .when(userService)
        .update(any());

    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(newEmail)
            .previousEmail(initialEmail)
            .build();

    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .time(1788495128493L)
            .type("UPDATE_PROFILE")
            .realmId("shopizer")
            .clientId("account")
            .userId(keycloakUserId)
            .ipAddress("172.19.0.1")
            .details(details)
            .build();

    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account.UPDATE_PROFILE";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Message dlqMessage = rabbitTemplate.receive(RabbitMqConfig.updateEmailDlq);
              assertThat(dlqMessage).isNotNull();
              String body = new String(dlqMessage.getBody());
              assertThat(body).contains(keycloakUserId.toString());
            });
  }

  @Test
  @DisplayName("Resilience & Idempotency: Duplicate events should succeed and keep email updated")
  void handle_Idempotency_DuplicateEvents_ShouldSucceed() {
    KeycloakUpdateEmailDetails details =
        KeycloakUpdateEmailDetails.builder()
            .context("ACCOUNT")
            .updatedEmail(newEmail)
            .previousEmail(initialEmail)
            .build();

    KeycloakUpdateEmailEvent event =
        KeycloakUpdateEmailEvent.builder()
            .time(1788495128493L)
            .type("UPDATE_PROFILE")
            .realmId("shopizer")
            .clientId("account")
            .userId(keycloakUserId)
            .ipAddress("172.19.0.1")
            .details(details)
            .build();

    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account.UPDATE_PROFILE";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> updatedUser = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(updatedUser).isPresent();
              assertThat(updatedUser.get().getEmail()).isEqualTo(newEmail);
              assertThat(updatedUser.get().getVerified()).isFalse();
            });

    // Send duplicate event
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> updatedUser = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(updatedUser).isPresent();
              assertThat(updatedUser.get().getEmail()).isEqualTo(newEmail);
              assertThat(updatedUser.get().getVerified()).isFalse();
            });
  }
}
