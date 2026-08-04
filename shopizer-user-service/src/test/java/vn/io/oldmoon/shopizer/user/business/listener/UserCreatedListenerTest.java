package vn.io.oldmoon.shopizer.user.business.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import vn.io.oldmoon.shopizer.user.business.event.UserCreatedEvent;
import vn.io.oldmoon.shopizer.user.business.event.listener.UserCreatedCreateProfileListener;
import vn.io.oldmoon.shopizer.user.business.service.CustomerService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

class UserCreatedListenerTest {
  @Mock private CustomerService customerService;

  @InjectMocks private UserCreatedCreateProfileListener listener;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void savedProfileShouldEqualToMessage() {
    // given
    UserCreatedEvent event =
        new UserCreatedEvent(
            UUID.randomUUID().toString(), "test@example.com", "testuser", "John", "Doe");

    CustomerProfile savedProfile = new CustomerProfile();

    when(customerService.createProfile(any())).thenReturn(savedProfile);

    // when
    listener.handle(event);

    // then
    ArgumentCaptor<CustomerProfile> captor = ArgumentCaptor.forClass(CustomerProfile.class);

    verify(customerService).createProfile(captor.capture());

    CustomerProfile passedProfile = captor.getValue();

    assertThat(passedProfile.getUserId().toString()).isEqualTo(event.userId());
    assertThat(passedProfile.getEmail()).isEqualTo(event.email());
    assertThat(passedProfile.getUsername()).isEqualTo(event.username());
    assertThat(passedProfile.getFirstName()).isEqualTo(event.firstName());
    assertThat(passedProfile.getLastName()).isEqualTo(event.lastName());
  }
}
