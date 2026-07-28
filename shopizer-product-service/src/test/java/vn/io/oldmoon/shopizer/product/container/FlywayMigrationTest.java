package vn.io.oldmoon.shopizer.product.container;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Slf4j
public class FlywayMigrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Test
  void migrationsApplySuccessfully() {
    Flyway flyway =
        Flyway.configure()
            .locations("classpath:/migration")
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .load();

    flyway.migrate();

    int appliedCount = flyway.info().applied().length;

    log.info("Migration applied: {}", appliedCount);

    assertEquals(appliedCount, flyway.info().all().length);
  }
}
