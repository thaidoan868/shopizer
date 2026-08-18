package vn.io.oldmoon.shopizer.user.container;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DatabaseSchemaValidationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  // prevent Keycloak from connecting to the failed keycloak container
  @MockitoBean private JwtDecoder jwtDecoder;

  @MockitoBean private Keycloak keycloak;

  @Test
  void contextLoadsAndSchemaMatchesEntities() {
    // If the Spring Context loads successfully, it means Flyway migrations ran
    // AND Hibernate validated that all @Entity mappings match the DB tables/columns.
  }
}
