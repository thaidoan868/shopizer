package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.business.event.keycloakAdminEvent.KeycloakAdminEvent;
import vn.io.oldmoon.shopizer.user.business.event.keycloakAdminEvent.KeycloakAdminEventParser;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminEventListenerTest {

  @Mock private KeycloakAdminEventParser eventParser;

  @Mock private UserService userService;

  @InjectMocks private KeycloakAdminUserCreatedEventListener listener;

  @Test
  @DisplayName("handle should parse event and persist user entity via UserService")
  void handle_ShouldParseAndPersistUser() {
    // Given
    UUID userId = UUID.randomUUID();
    KeycloakAdminEvent event = mock(KeycloakAdminEvent.class);
    User userToSave =
        User.builder().keycloakUserId(userId).username("napoleon").email("napoleon@france").build();
    User savedUser =
        User.builder().keycloakUserId(userId).username("napoleon").email("napoleon@france").build();

    when(eventParser.toUserEntity(event)).thenReturn(userToSave);
    when(userService.create(userToSave)).thenReturn(savedUser);

    // When
    listener.handle(event);

    // Then
    verify(eventParser).toUserEntity(event);
    verify(userService).create(userToSave);
  }

  @Test
  @DisplayName("handle should propagate exception when event parser fails")
  void handle_WhenEventParserFails_ShouldPropagateException() {
    // Given
    KeycloakAdminEvent event = mock(KeycloakAdminEvent.class);
    when(eventParser.toUserEntity(event))
        .thenThrow(new InvalidInputException("Malformed representation"));

    // When & Then
    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(InvalidInputException.class)
        .hasMessageContaining("Malformed representation");
  }
}
