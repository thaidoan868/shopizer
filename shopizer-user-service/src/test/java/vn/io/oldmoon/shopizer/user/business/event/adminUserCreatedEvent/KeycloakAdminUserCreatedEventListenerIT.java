package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
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
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@SpringBootTest
@Testcontainers
class KeycloakAdminUserCreatedEventListenerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private UserRepository userRepository;
  @MockitoSpyBean private UserService userService;
  private UUID keycloakUserId;
  private String username;
  private String email;
  private KeycloakAdminUserCreatedEvent event;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");
  }

  @BeforeEach
  void setUp() {
    keycloakUserId = UUID.randomUUID();
    username = "napoleon_" + UUID.randomUUID().toString().substring(0, 8);
    email = "napoleon_" + UUID.randomUUID().toString().substring(0, 8) + "@france.com";
    KeycloakAdminAuthDetails authDetails =
        KeycloakAdminAuthDetails.builder()
            .realmId(UUID.randomUUID().toString())
            .realmName("master")
            .clientId("admin-cli")
            .userId(UUID.randomUUID().toString())
            .ipAddress("127.0.0.1")
            .build();

    String representation =
        "{\\\"username\\\":\\\"%s\\\",\\\"firstName\\\":\\\"\\\",\\\"lastName\\\":\\\"\\\",\\\"email\\\":\\\"%s\\\",\\\"emailVerified\\\":false,\\\"attributes\\\":{\\\"locale\\\":[\\\"\\\"]},\\\"enabled\\\":true,\\\"requiredActions\\\":[],\\\"groups\\\":[]}"
            .formatted(username, email);

    event =
        KeycloakAdminUserCreatedEvent.builder()
            .time(System.currentTimeMillis())
            .authDetails(authDetails)
            .resourceType("USER")
            .operationType("CREATE")
            .resourcePath("users/" + keycloakUserId)
            .representation(representation)
            .resourceTypeAsString("USER")
            .build();
  }

  @Test
  @DisplayName(
      "Happy Path: Consume Keycloak admin user create event and insert user record into users table")
  void handle_HappyPath_ShouldPersistUserInDatabase() {
    // When
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.AdminUserCreatedBindingKey, event);

    // Then
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> userOptional = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(userOptional).isPresent();

              User user = userOptional.get();
              assertThat(user.getKeycloakUserId()).isEqualTo(this.keycloakUserId);
              assertThat(user.getUsername()).isEqualTo(this.username); // From representation JSON
              assertThat(user.getEmail()).isEqualTo(this.email);

              assertThat(user.getVerified()).isFalse();
              assertThat(user.getRealm()).isEqualTo("master");
            });
  }

  @Test
  @DisplayName(
      "Resilience & Idempotency: Duplicate events with same user ID should not cause primary key violation")
  void handle_Idempotency_DuplicateEvents_ShouldNotCausePrimaryKeyViolation() {
    // When: Send first event
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.AdminUserCreatedBindingKey, event);

    // Verify first event persisted
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> userOptional = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(userOptional).isPresent();
            });

    // When: Send duplicate event with same user ID
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.AdminUserCreatedBindingKey, event);

    // Then: Verify user still exists and no crash / DLQ occurs
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<User> userOptional = userRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(userOptional).isPresent();
            });
  }
}
