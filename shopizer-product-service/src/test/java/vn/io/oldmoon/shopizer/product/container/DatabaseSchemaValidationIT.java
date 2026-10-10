package vn.io.oldmoon.shopizer.product.container;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DatabaseSchemaValidationIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Test
  void contextLoadsAndSchemaMatchesEntities() {
    // If the Spring Context loads successfully, it means Flyway migrations ran
    // AND Hibernate validated that all @Entity mappings match the DB tables/columns.
  }
}
