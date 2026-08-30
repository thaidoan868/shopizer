package vn.io.oldmoon.shopizer.user.app.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@ExtendWith(MockitoExtension.class)
class CustomerPopulatorTest {

  @Mock private UserPopulator userPopulator;

  @InjectMocks private CustomerPopulator customerPopulator;

  @Test
  @DisplayName("CustomerPopulator should delegate to UserPopulator for toCustomerProfileDto")
  void toCustomerProfileDto_ShouldDelegateToUserPopulator() {
    // Given
    User user = User.builder().keycloakUserId(UUID.randomUUID()).username("charlie").build();
    CustomerProfile profile = CustomerProfile.builder().user(user).build();
    CustomerProfileDto expectedDto = CustomerProfileDto.builder().username("charlie").build();

    given(userPopulator.toCustomerProfileDto(user, profile)).willReturn(expectedDto);

    // When
    CustomerProfileDto result = customerPopulator.toCustomerProfileDto(user, profile);

    // Then
    assertThat(result).isSameAs(expectedDto);
    verify(userPopulator).toCustomerProfileDto(user, profile);
  }

  @Test
  @DisplayName("CustomerPopulator should delegate to UserPopulator for toAvatarDto")
  void toAvatarDto_ShouldDelegateToUserPopulator() {
    // Given
    AvatarMeta avatarMeta = new AvatarMeta("bucket", "o.png", "m.png", "t.png");
    AvatarDto expectedAvatarDto =
        new AvatarDto("http://cdn/o.png", "http://cdn/m.png", "http://cdn/t.png");

    given(userPopulator.toAvatarDto(avatarMeta)).willReturn(expectedAvatarDto);

    // When
    AvatarDto result = customerPopulator.toAvatarDto(avatarMeta);

    // Then
    assertThat(result).isSameAs(expectedAvatarDto);
    verify(userPopulator).toAvatarDto(avatarMeta);
  }
}
