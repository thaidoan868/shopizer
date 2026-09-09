package vn.io.oldmoon.shopizer.user.app.health;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KeycloakHealthIndicator implements HealthIndicator {
  private final Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.server-url}")
  private String serverUrl;

  @Override
  public Health health() {
    RestClient restClient = RestClient.builder().build();
    String discoveryUrl = serverUrl + "/realms/" + realm + "/.well-known/openid-configuration";

    try {
      restClient.get().uri(discoveryUrl).retrieve().toBodilessEntity();

      Integer userCount = keycloak.realm(realm).users().count();

      return Health.up()
          .withDetail("network", "Available")
          .withDetail("authentication", "Valid Credentials")
          .withDetail("realmUrl", discoveryUrl)
          .withDetail("userCount", userCount)
          .build();

    } catch (Exception e) {
      return Health.down()
          .withDetail("keycloak", "Unreachable or Unauthorized")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
