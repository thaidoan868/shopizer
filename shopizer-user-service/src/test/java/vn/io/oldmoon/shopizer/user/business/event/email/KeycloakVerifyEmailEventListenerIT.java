package vn.io.oldmoon.shopizer.user.business.event.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class KeycloakVerifyEmailEventListenerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private UserRepository userRepository;
  @MockitoSpyBean private UserService userService;

  private String email;
  private String username;
  private UUID keycloakUserId;
  private User user;
  private KeycloakVerifyEmailEvent event;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
  }

  @BeforeEach
  void setUp() {
    this.keycloakUserId = UUID.randomUUID();
    this.username = "lux-" + UUID.randomUUID().toString().substring(0, 8);
    this.email = "myquyen-" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com";
    this.user =
        User.builder()
            .keycloakUserId(keycloakUserId)
            .username(username)
            .email(email)
            .verified(false)
            .build();
    userRepository.save(user);

    KeycloakVerifyEmailDetails details =
        KeycloakVerifyEmailDetails.builder()
            .authMethod("openid-connect")
            .tokenId("efe25f18-53e3-4be8-a0c2-04daeefb79cd")
            .action("verify-email")
            .responseType("code")
            .redirectUri("http://localhost:8080/realms/shopizer/account")
            .rememberMe("false")
            .codeId("7af05dea-a99a-4ab7-8698-1569465d3380")
            .email(email)
            .responseMode("query")
            .username(username)
            .build();

    this.event =
        KeycloakVerifyEmailEvent.builder()
            .time(1788493745127L)
            .type("VERIFY_EMAIL")
            .realmId("a9cbd686-1d46-44c5-9c61-26078d493828")
            .clientId("account-console")
            .userId(keycloakUserId)
            .ipAddress("172.19.0.1")
            .details(details)
            .build();
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(userService);
  }

  @Test
  @DisplayName("Happy Path: Receive VERIFY_EMAIL event, update user verified status to true")
  void handle_HappyPath_ShouldUpdateUserVerifiedToTrue() {
    // When: Publish event with routing key matching KK.EVENT.CLIENT.shopizer.SUCCESS.*.VERIFY_EMAIL
    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account-console.VERIFY_EMAIL";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    // Then: Verify user in DB has verified = true
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> updatedUser = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(updatedUser).isPresent();
              assertThat(updatedUser.get().getVerified()).isTrue();
              assertThat(updatedUser.get().getEmail()).isEqualTo(email);
            });
  }

  @Test
  @DisplayName("Bad Path: When user update fails, message should be routed to Dead Letter Queue")
  void handle_WhenProcessingFails_ShouldRouteToDeadLetterQueue() {
    doThrow(new RuntimeException("Simulated database failure"))
        .when(userService)
        .verifyEmail(eq(keycloakUserId), any());

    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account-console.VERIFY_EMAIL";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Message dlqMessage = rabbitTemplate.receive(RabbitMqConfig.verifyEmailDlq);
              assertThat(dlqMessage).isNotNull();
              String body = new String(dlqMessage.getBody());
              assertThat(body).contains(keycloakUserId.toString());
            });
  }

  @Test
  @DisplayName("Resilience & Idempotency: Duplicate events should succeed and keep verified = true")
  void handle_Idempotency_DuplicateEvents_ShouldSucceed() {
    String routingKey = "KK.EVENT.CLIENT.shopizer.SUCCESS.account-console.VERIFY_EMAIL";
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> updatedUser = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(updatedUser).isPresent();
              assertThat(updatedUser.get().getVerified()).isTrue();
            });

    // Send duplicate event
    rabbitTemplate.convertAndSend(RabbitMqConfig.userEventExchange, routingKey, event);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> updatedUser = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(updatedUser).isPresent();
              assertThat(updatedUser.get().getVerified()).isTrue();
            });
  }
}
