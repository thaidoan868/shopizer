package vn.io.oldmoon.shopizer.user.business.event.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.event.RabbitMqEventPublisher;
import vn.io.oldmoon.shopizer.user.business.service.profile.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@ExtendWith(MockitoExtension.class)
class KeycloakUserRegisteredEventListenerTest {

  @Mock private CustomerProfileService customerProfileService;

  @Mock private KeycloakUserRegisteredEventParser keycloakUserRegisteredEventParser;

  @Mock private RabbitMqEventPublisher eventPublisher;

  @InjectMocks private KeycloakUserRegisteredEventListener listener;

  @Test
  @DisplayName(
      "handle should convert event, create customer profile, and publish CustomerCreatedEvent")
  void handle_ShouldProcessRegistrationAndPublishEvent() {
    // Given
    UUID keycloakUserId = UUID.randomUUID();
    KeycloakUserRegisteredEvent event = mock(KeycloakUserRegisteredEvent.class);
    User user = User.builder().keycloakUserId(keycloakUserId).build();
    CustomerProfile customerProfile = CustomerProfile.builder().user(user).build();

    when(event.userId()).thenReturn(keycloakUserId);
    when(keycloakUserRegisteredEventParser.toUserEntity(event)).thenReturn(user);
    when(customerProfileService.create(user)).thenReturn(customerProfile);

    // When
    listener.handle(event);

    // Then
    verify(keycloakUserRegisteredEventParser).toUserEntity(event);
    verify(customerProfileService).create(user);

    // Capture published event & routing key
    ArgumentCaptor<CustomerCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerCreatedEvent.class);
    ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

    verify(eventPublisher).publish(eventCaptor.capture(), routingKeyCaptor.capture());

    assertThat(eventCaptor.getValue()).isNotNull();
    assertThat(eventCaptor.getValue().keycloakUserId()).isEqualTo(keycloakUserId.toString());
    assertThat(routingKeyCaptor.getValue()).isEqualTo(RabbitMqConfig.customerCreatedBindingKey);
  }
}
