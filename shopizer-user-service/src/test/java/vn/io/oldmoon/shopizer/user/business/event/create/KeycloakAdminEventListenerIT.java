package vn.io.oldmoon.shopizer.user.business.event.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminAuthDetails;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileRepository;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@SpringBootTest
@Testcontainers
class KeycloakAdminEventListenerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private EmployeeProfileRepository employeeProfileRepository;
  @MockitoSpyBean private UserService userService;
  @MockitoSpyBean private EmployeeProfileService employeeProfileService;

  @MockitoSpyBean
  private KeycloakAdminUserCreatedEventListener keycloakAdminUserCreatedEventListener;

  private UUID keycloakUserId;
  private UUID creatorId;
  private String username;
  private String email;
  private KeycloakAdminEvent event;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");
  }

  @BeforeEach
  void setUp() {
    keycloakUserId = UUID.randomUUID();
    creatorId = UUID.randomUUID();
    username = "napoleon_" + UUID.randomUUID().toString().substring(0, 8);
    email = "napoleon_" + UUID.randomUUID().toString().substring(0, 8) + "@france.com";
    KeycloakAdminAuthDetails authDetails =
        KeycloakAdminAuthDetails.builder()
            .realmId(UUID.randomUUID().toString())
            .realmName("master")
            .clientId("admin-cli")
            .userId(creatorId.toString())
            .ipAddress("127.0.0.1")
            .build();

    String representation =
        """
        {"username":"%s","firstName":"","lastName":"","email":"%s","emailVerified":false,"attributes":{"locale":[""]},"enabled":true,"requiredActions":[],"groups":[]}
        """
            .formatted(username, email);

    event =
        KeycloakAdminEvent.builder()
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
      "Happy Path: Consume Keycloak admin user create event and insert user and employee profile records")
  void handle_HappyPath_ShouldPersistUserAndEmployeeProfileInDatabase() {
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
              assertThat(user.getRealm()).isEqualTo("shopizer");
              assertThat(user.getCreatedBy()).isEqualTo(creatorId);

              Optional<EmployeeProfileQueryDto> profileDto =
                  employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(profileDto).isPresent();

              Optional<EmployeeProfile> profileOptional =
                  employeeProfileRepository.findById(profileDto.get().id());
              assertThat(profileOptional).isPresent();

              EmployeeProfile profile = profileOptional.get();
              assertThat(profile.getUser().getId()).isEqualTo(user.getId());
              assertThat(profile.getCreatedBy()).isEqualTo(creatorId);
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

              Optional<EmployeeProfileQueryDto> profileDto =
                  employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(profileDto).isPresent();
            });

    // When: Send duplicate event with same user ID
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.AdminUserCreatedBindingKey, event);

    // Verify the listener method was invoked 2 times total without throwing an exception
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> verify(keycloakAdminUserCreatedEventListener, times(2)).handle(any()));

    // Verify only one employee profile exists for the user
    Optional<EmployeeProfileQueryDto> profileDto =
        employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
    assertThat(profileDto).isPresent();
  }

  @Test
  @DisplayName(
      "Transactional: When profile creation fails, the transaction rolls back and no user is persisted")
  void handle_WhenProfileCreationFails_ShouldRollbackUserCreation() {
    doThrow(new RuntimeException("Simulated profile creation failure"))
        .when(employeeProfileService)
        .create(any(EmployeeProfile.class));

    // When: Send event
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange, RabbitMqConfig.AdminUserCreatedBindingKey, event);

    // Verify handler was invoked
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> verify(keycloakAdminUserCreatedEventListener, times(1)).handle(any()));

    // Verify user was NOT persisted due to transaction rollback
    Optional<User> userOptional = userRepository.findByKeycloakUserId(keycloakUserId);
    assertThat(userOptional).isEmpty();
  }
}

