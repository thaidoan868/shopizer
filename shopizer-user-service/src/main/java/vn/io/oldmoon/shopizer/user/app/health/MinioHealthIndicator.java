package vn.io.oldmoon.shopizer.user.app.health;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioHealthIndicator implements HealthIndicator {

  private final MinioClient minioClient;

  @Override
  public Health health() {
    try {
      // Triggers a lightweight API call to test both the network and the Access Keys
      int bucketCount = minioClient.listBuckets().size();

      return Health.up()
          .withDetail("network", "Available")
          .withDetail("authentication", "Valid Access Keys")
          .withDetail("bucketCount", bucketCount)
          .build();

    } catch (ErrorResponseException e) {
      // MinIO throws this specific exception for HTTP 403 (Invalid credentials/policies)
      return Health.down()
          .withDetail("minio", "Unauthorized")
          .withDetail("network", "Available")
          .withDetail("error", "Invalid Access Key or Secret Key: " + e.errorResponse().code())
          .build();

    } catch (Exception e) {
      // Catch-all for java.net.ConnectException (Unreachable) and other timeouts
      return Health.down()
          .withDetail("minio", "Unreachable or Execution Failed")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
