package vn.io.oldmoon.shopizer.user.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.infra.data.constant.FileStatus;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;
import vn.io.oldmoon.shopizer.user.infra.repository.FileMetaRepository;

@SpringBootTest
@Testcontainers
class FileMetaServiceIT {

  private static final String TEST_BUCKET = "test-bucket";

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static GenericContainer<?> minio =
      new GenericContainer<>("minio/minio:RELEASE.2025-07-23T15-54-02Z")
          .withEnv("MINIO_ROOT_USER", "minioadmin")
          .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
          .withCommand("server", "/data")
          .withExposedPorts(9000)
          .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private Keycloak keycloak;

  @Autowired private FileMetaService fileMetaService;
  @Autowired private FileMetaRepository fileMetaRepository;
  @Autowired private MinioClient minioClient;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "minio.endpoint",
        () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
    registry.add("minio.access-key", () -> "minioadmin");
    registry.add("minio.secret-key", () -> "minioadmin");
    registry.add("minio.public-bucket", () -> TEST_BUCKET);
    registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
  }

  @BeforeEach
  void setUp() throws Exception {
    boolean bucketExists =
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(TEST_BUCKET).build());
    if (!bucketExists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(TEST_BUCKET).build());
    }
  }

  @Test
  @DisplayName("Happy Path: save stream payload uploads to MinIO and saves metadata to database")
  void save_WhenValidStream_ShouldUploadToMinioAndPersistInDb() throws Exception {
    // Given
    byte[] payload = "Hello MinIO and Postgres".getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = new ByteArrayInputStream(payload);
    String objectName = "test-bytes-" + UUID.randomUUID() + ".txt";
    FileMeta fileMeta =
        FileMeta.builder()
            .bucket(TEST_BUCKET)
            .objectName(objectName)
            .sizeBytes((long) payload.length)
            .contentType("text/plain")
            .visibility(Visibility.PUBLIC)
            .build();

    // When
    FileMeta saved = fileMetaService.save(inputStream, fileMeta);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(FileStatus.ACTIVE);
    assertThat(saved.getSizeBytes()).isEqualTo((long) payload.length);
    assertThat(saved.getBucket()).isEqualTo(TEST_BUCKET);
    assertThat(saved.getObjectName()).isEqualTo(objectName);

    // Verify in database
    Optional<FileMeta> inDb = fileMetaRepository.findById(saved.getId());
    assertThat(inDb).isPresent();
    assertThat(inDb.get().getStatus()).isEqualTo(FileStatus.ACTIVE);
    assertThat(inDb.get().getObjectName()).isEqualTo(objectName);

    // Verify in MinIO
    try (InputStream is =
        minioClient.getObject(
            GetObjectArgs.builder().bucket(TEST_BUCKET).object(objectName).build())) {
      byte[] downloaded = is.readAllBytes();
      assertThat(downloaded).isEqualTo(payload);
    }
  }

  @Test
  @DisplayName("Happy Path: save stream without explicit size uploads and persists correctly")
  void saveStream_WithoutExplicitSize_ShouldUploadAndPersist() throws Exception {
    // Given
    byte[] payload = "Stream content without explicit size".getBytes(StandardCharsets.UTF_8);
    String objectName = "test-stream-nosize-" + UUID.randomUUID() + ".txt";
    InputStream inputStream = new ByteArrayInputStream(payload);
    FileMeta fileMeta =
        FileMeta.builder()
            .bucket(TEST_BUCKET)
            .objectName(objectName)
            .contentType("text/plain")
            .visibility(Visibility.PUBLIC)
            .build();

    // When
    FileMeta saved = fileMetaService.save(inputStream, fileMeta);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(FileStatus.ACTIVE);

    // Verify in MinIO
    try (InputStream is =
        minioClient.getObject(
            GetObjectArgs.builder().bucket(TEST_BUCKET).object(objectName).build())) {
      byte[] downloaded = is.readAllBytes();
      assertThat(downloaded).isEqualTo(payload);
    }
  }

  @Test
  @DisplayName("delete: soft deletes file metadata in DB while keeping file in storage")
  void delete_ShouldSoftDeleteInDbAndKeepFileInStorage() throws Exception {
    // Given: first upload a file
    byte[] payload = "file to be deleted".getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = new ByteArrayInputStream(payload);
    String objectName = "to-delete-" + UUID.randomUUID() + ".txt";
    FileMeta fileMeta =
        FileMeta.builder()
            .bucket(TEST_BUCKET)
            .objectName(objectName)
            .contentType("text/plain")
            .visibility(Visibility.PUBLIC)
            .build();

    FileMeta saved = fileMetaService.save(inputStream, fileMeta);
    assertThat(saved.getStatus()).isEqualTo(FileStatus.ACTIVE);

    // When: delete is called
    fileMetaService.delete(TEST_BUCKET, objectName);

    // Then: status is updated to DELETED in DB
    Optional<FileMeta> inDb = fileMetaRepository.findById(saved.getId());
    assertThat(inDb).isPresent();
    assertThat(inDb.get().getStatus()).isEqualTo(FileStatus.DELETED);

    // And: file still exists in MinIO storage
    try (InputStream is =
        minioClient.getObject(
            GetObjectArgs.builder().bucket(TEST_BUCKET).object(objectName).build())) {
      byte[] downloaded = is.readAllBytes();
      assertThat(downloaded).isEqualTo(payload);
    }
  }

  @Test
  @DisplayName("delete: throws ResourceNotFoundException when file does not exist")
  void delete_WhenFileDoesNotExist_ShouldThrowResourceNotFoundException() {
    String nonExistentObjectName = "does-not-exist-" + UUID.randomUUID();

    assertThatThrownBy(() -> fileMetaService.delete(TEST_BUCKET, nonExistentObjectName))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("File metadata");
  }

  @Test
  @DisplayName("Unique constraint: duplicate bucket and objectName violates unique constraint")
  void save_WhenDuplicateBucketAndObjectName_ShouldThrowDataIntegrityViolationException() {
    // Given
    String objectName = "duplicate-" + UUID.randomUUID() + ".txt";
    byte[] payload = "first version".getBytes(StandardCharsets.UTF_8);
    FileMeta first =
        FileMeta.builder()
            .bucket(TEST_BUCKET)
            .objectName(objectName)
            .contentType("text/plain")
            .build();
    fileMetaService.save(new ByteArrayInputStream(payload), first);

    // When & Then
    FileMeta second =
        FileMeta.builder()
            .bucket(TEST_BUCKET)
            .objectName(objectName)
            .contentType("text/plain")
            .build();
    assertThatThrownBy(
            () -> fileMetaService.save(new ByteArrayInputStream(payload), second))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
