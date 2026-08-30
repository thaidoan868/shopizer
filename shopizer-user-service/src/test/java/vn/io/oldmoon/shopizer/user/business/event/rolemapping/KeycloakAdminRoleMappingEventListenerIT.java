package vn.io.oldmoon.shopizer.user.business.event.rolemapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
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
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileQueryDto;
import vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileRepository;
import vn.io.oldmoon.shopizer.user.infra.repository.UserRepository;

@SpringBootTest
@Testcontainers
class KeycloakAdminRoleMappingEventListenerIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private RabbitTemplate rabbitTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private EmployeeProfileRepository employeeProfileRepository;

  @MockitoSpyBean
  private KeycloakAdminRoleMappingEventListener keycloakAdminRoleMappingEventListener;

  private UUID keycloakUserId;
  private UUID adminId;
  private User savedUser;
  private KeycloakAdminEvent event;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.listener.simple.default-requeue-rejected", () -> "false");
  }

  @BeforeEach
  void setUp() {
    keycloakUserId = UUID.randomUUID();
    adminId = UUID.randomUUID();

    savedUser =
        userRepository.save(
            User.builder()
                .keycloakUserId(keycloakUserId)
                .username("admin_user_" + UUID.randomUUID().toString().substring(0, 8))
                .email("admin_user_" + UUID.randomUUID().toString().substring(0, 8) + "@shopizer.com")
                .firstName("John")
                .lastName("Doe")
                .verified(true)
                .build());

    KeycloakAdminAuthDetails authDetails =
        KeycloakAdminAuthDetails.builder()
            .realmId(UUID.randomUUID().toString())
            .realmName("master")
            .clientId("admin-cli")
            .userId(adminId.toString())
            .ipAddress("172.19.0.1")
            .build();

    String representation =
        """
        [{"id":"2b230619-bcfc-4ae4-b608-954a4b185290","name":"SUPER_ADMIN","description":"Full System Access: Manage personnel...","composite":false,"clientRole":false,"containerId":"a9cbd686-1d46-44c5-9c61-26078d493828"}]
        """;

    event =
        KeycloakAdminEvent.builder()
            .time(System.currentTimeMillis())
            .realmId("a9cbd686-1d46-44c5-9c61-26078d493828")
            .authDetails(authDetails)
            .resourceType("REALM_ROLE_MAPPING")
            .operationType("CREATE")
            .resourcePath("users/" + keycloakUserId + "/role-mappings/realm")
            .representation(representation)
            .resourceTypeAsString("REALM_ROLE_MAPPING")
            .build();
  }

  @Test
  @DisplayName(
      "Happy Path: Consume Keycloak admin realm role mapping event and insert employee profile record")
  void handle_HappyPath_ShouldPersistEmployeeProfileInDatabase() {
    // When
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange,
        RabbitMqConfig.adminRoleMappingCreatedBindingKey,
        event);

    // Then
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<EmployeeProfileQueryDto> profileDto =
                  employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(profileDto).isPresent();

              Optional<EmployeeProfile> profileOptional =
                  employeeProfileRepository.findById(profileDto.get().id());
              assertThat(profileOptional).isPresent();

              EmployeeProfile profile = profileOptional.get();
              assertThat(profile.getUser().getId()).isEqualTo(savedUser.getId());
              assertThat(profile.getCreatedBy()).isEqualTo(adminId);
            });
  }

  @Test
  @DisplayName(
      "Resilience & Idempotency: Duplicate events for same userId should not fail and should not create duplicates")
  void handle_Idempotency_DuplicateEvents_ShouldNotCauseFailure() {
    // When: Send first event
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange,
        RabbitMqConfig.adminRoleMappingCreatedBindingKey,
        event);

    // Verify first event persisted
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Optional<EmployeeProfileQueryDto> profileDto =
                  employeeProfileRepository.findByKeycloakUserId(keycloakUserId);
              assertThat(profileDto).isPresent();
            });

    // When: Send duplicate event with same userId
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange,
        RabbitMqConfig.adminRoleMappingCreatedBindingKey,
        event);

    // Verify the listener method was invoked 2 times total without throwing an exception
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> verify(keycloakAdminRoleMappingEventListener, times(2)).handle(any()));
  }

  @Test
  @DisplayName(
      "Non-Trigger Role: Events with only non-trigger roles should not create an employee profile")
  void handle_NonTriggerRole_ShouldNotCreateEmployeeProfile() {
    UUID nonEmployeeUserId = UUID.randomUUID();
    userRepository.save(
        User.builder()
            .keycloakUserId(nonEmployeeUserId)
            .username("customer_" + UUID.randomUUID().toString().substring(0, 8))
            .email("customer_" + UUID.randomUUID().toString().substring(0, 8) + "@shopizer.com")
            .build());

    String nonTriggerRep =
        """
        [{"id":"1a120508-abeb-3bd3-a507-84393b074179","name":"CUSTOMER","description":"Customer Role","composite":false,"clientRole":false,"containerId":"a9cbd686-1d46-44c5-9c61-26078d493828"}]
        """;

    KeycloakAdminEvent nonTriggerEvent =
        KeycloakAdminEvent.builder()
            .time(System.currentTimeMillis())
            .realmId("a9cbd686-1d46-44c5-9c61-26078d493828")
            .authDetails(event.authDetails())
            .resourceType("REALM_ROLE_MAPPING")
            .operationType("CREATE")
            .resourcePath("users/" + nonEmployeeUserId + "/role-mappings/realm")
            .representation(nonTriggerRep)
            .resourceTypeAsString("REALM_ROLE_MAPPING")
            .build();

    // When
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.userEventExchange,
        RabbitMqConfig.adminRoleMappingCreatedBindingKey,
        nonTriggerEvent);

    // Verify listener was invoked
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> verify(keycloakAdminRoleMappingEventListener, times(1)).handle(any()));

    // Verify no employee profile was created
    Optional<EmployeeProfileQueryDto> profileDto =
        employeeProfileRepository.findByKeycloakUserId(nonEmployeeUserId);
    assertThat(profileDto).isEmpty();
  }
}
