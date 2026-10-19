package vn.io.oldmoon.shopizer.product.app.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

  @Bean
  public MinioClient minioClient(
      @Value("${minio.endpoint}") String endpoint,
      @Value("${minio.access-key}") String accessKey,
      @Value("${minio.secret-key}") String secretKey) {
    return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
  }

  @Bean(name = "mediaBucketName")
  public String getMediaBucketName(
      @Value("${minio.bucket.media-public:media-public}") String mediaBucketName) {
    return mediaBucketName;
  }
}
