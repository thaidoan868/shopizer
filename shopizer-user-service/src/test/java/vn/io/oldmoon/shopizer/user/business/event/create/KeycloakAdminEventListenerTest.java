package vn.io.oldmoon.shopizer.user.business.event.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminAuthDetails;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.event.keycloakadmin.KeycloakAdminEventParser;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminEventListenerTest {

  @Mock private KeycloakAdminEventParser parser;

  @Mock private UserService userService;

  @InjectMocks private KeycloakAdminUserCreatedEventListener listener;

  @Nested
  @DisplayName("handle method tests")
  class HandleTests {

    @Test
    @DisplayName("handle should parse event and persist user entity via UserService")
    void handle_ShouldParseAndPersistUser() {
      // Given
      UUID userId = UUID.randomUUID();
      UUID creatorId = UUID.randomUUID();
      KeycloakAdminAuthDetails authDetails =
          KeycloakAdminAuthDetails.builder().userId(creatorId.toString()).build();
      KeycloakAdminEvent event =
          KeycloakAdminEvent.builder()
              .resourcePath("users/" + userId)
              .authDetails(authDetails)
              .build();

      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username("napoleon")
              .email("napoleon@france")
              .firstName("Napoleon")
              .lastName("Bonaparte")
              .emailVerified(true)
              .build();

      when(parser.extractUserId(event)).thenReturn(userId);
      when(parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);
      when(userService.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      listener.handle(event);

      // Then
      verify(parser).extractUserId(event);
      verify(parser).parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class);
      verify(userService)
          .create(
              argThat(
                  user ->
                      user.getKeycloakUserId().equals(userId)
                          && "napoleon".equals(user.getUsername())
                          && "napoleon@france".equals(user.getEmail())
                          && "Napoleon".equals(user.getFirstName())
                          && "Bonaparte".equals(user.getLastName())
                          && Boolean.TRUE.equals(user.getVerified())
                          && creatorId.equals(user.getCreatedBy())));
    }

  }

  @Nested
  @DisplayName("toUserEntity mapping tests")
  class ToUserEntityTests {

    private UUID userId;
    private UUID creatorId;
    private KeycloakAdminEvent event;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      creatorId = UUID.randomUUID();
      KeycloakAdminAuthDetails authDetails =
          KeycloakAdminAuthDetails.builder().userId(creatorId.toString()).build();
      event =
          KeycloakAdminEvent.builder()
              .resourcePath("users/" + userId)
              .authDetails(authDetails)
              .build();
      when(parser.extractUserId(event)).thenReturn(userId);
    }

    @Test
    @DisplayName("toUserEntity should map all representation and event fields correctly")
    void toUserEntity_WithValidEventAndAuthDetails_ShouldMapAllFields() {
      KeycloakAdminUserCreatedRepresentation rep =
          KeycloakAdminUserCreatedRepresentation.builder()
              .username(" napoleon ")
              .email(" napoleon@france ")
              .firstName("Napoleon")
              .lastName("Bonaparte")
              .emailVerified(true)
              .build();

      when(parser.parseRepresentation(event, KeycloakAdminUserCreatedRepresentation.class))
          .thenReturn(rep);

      User user = listener.toUserEntity(event);

      assertThat(user).isNotNull();
      assertThat(user.getKeycloakUserId()).isEqualTo(userId);
      assertThat(user.getUsername()).isEqualTo("napoleon");
      assertThat(user.getEmail()).isEqualTo("napoleon@france");
      assertThat(user.getFirstName()).isEqualTo("Napoleon");
      assertThat(user.getLastName()).isEqualTo("Bonaparte");
      assertThat(user.getVerified()).isTrue();
      assertThat(user.getCreatedBy()).isEqualTo(creatorId);
    }
  }
}
