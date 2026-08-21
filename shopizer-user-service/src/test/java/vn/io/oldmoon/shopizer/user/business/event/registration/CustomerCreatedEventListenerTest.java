package vn.io.oldmoon.shopizer.user.business.event.registration;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.business.service.keycloak.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@ExtendWith(MockitoExtension.class)
class CustomerCreatedEventListenerTest {

  @Mock private KeycloakService keycloakService;

  private CustomerCreatedEvent event;

  @InjectMocks private CustomerCreatedEventListener listener;

  @Test
  @DisplayName("handle should assign CUSTOMER realm role via keycloakService")
  void handle_ShouldAssignCustomerRoleToUser() {
    // Given
    String keycloakUserId = UUID.randomUUID().toString();
    event = new CustomerCreatedEvent(keycloakUserId);

    // When
    listener.handle(event);

    // Then
    verify(keycloakService).assignRealmRole(keycloakUserId, Role.CUSTOMER);
  }
}
