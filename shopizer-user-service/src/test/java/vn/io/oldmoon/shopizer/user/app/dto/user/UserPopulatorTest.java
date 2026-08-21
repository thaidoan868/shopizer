package vn.io.oldmoon.shopizer.user.app.dto.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.business.event.registration.KeycloakUserRegisteredEvent;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@ExtendWith(MockitoExtension.class)
class UserPopulatorTest {

  @Mock private UserMapper userMapper;

  @Mock private KeycloakUserRegisteredEvent event;

  @InjectMocks private UserPopulator userPopulator;

  @Test
  void toUserEntity_ShouldDelegateToUserMapperAndReturnUser() {
    // Given
    UUID userId = UUID.randomUUID();
    User expectedUser = User.builder().keycloakUserId(userId).username("johndoe").build();

    when(userMapper.toUserEntity(event)).thenReturn(expectedUser);

    // When
    User actualUser = userPopulator.toUserEntity(event);

    // Then
    assertThat(actualUser).isNotNull().isEqualTo(expectedUser);
    verify(userMapper).toUserEntity(event);
  }
}
