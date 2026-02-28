package vn.io.oldmoon.shopizer.user.app.populator.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.infra.model.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@SpringBootTest
class CustomerPopulatorTest {
  @Autowired private CustomerPopulator populator;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("minio.endpoint", () -> "http://localhost:9000");
  }

  @Test
  void toProfileResponse() {
    // given
    String email = "testing@gmail.com";
    String username = "testing";
    String firstName = "testFirstName";
    String lastName = "testLastName";
    AvatarMeta avatarMeta =
        new AvatarMeta(
            "public-test-assets", "originalObjectName", "mediumObjectName", "thumbnailName");
    CustomerProfile customerProfile =
        CustomerProfile.builder()
            .email(email)
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .avatarMeta(avatarMeta)
            .build();

    // when
    CustomerProfileResponse response = populator.toProfileResponse(customerProfile);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getEmail()).isEqualTo(email);
    assertThat(response.getUsername()).isEqualTo(username);
    assertThat(response.getFirstName()).isEqualTo(firstName);
    assertThat(response.getLastName()).isEqualTo(lastName);

    assertThat(response.getAvatarMeta().mediumAvatarUrl())
        .contains("http://localhost:9000")
        .contains("public-test-assets")
        .contains("mediumObjectName");
  }
}
