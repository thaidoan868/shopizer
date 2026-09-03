package vn.io.oldmoon.shopizer.user.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.user.infra.data.constant.FileStatus;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;
import vn.io.oldmoon.shopizer.user.infra.repository.FileMetaRepository;

@ExtendWith(MockitoExtension.class)
class FileMetaServiceTest {

  @Mock private MinioClient minioClient;
  @Mock private FileMetaRepository fileMetaRepository;

  @InjectMocks private FileMetaService fileMetaService;

  @Nested
  @DisplayName("save(InputStream payload, FileMeta fileMeta)")
  class SaveStreamTest {

    @Test
    @DisplayName("should upload to MinIO and persist file entity when input is valid")
    void save_WhenValidInput_ShouldUploadAndPersist() throws Exception {
      // Given
      InputStream payload = new ByteArrayInputStream("image-bytes".getBytes());
      FileMeta fileMeta =
          FileMeta.builder()
              .bucket("test-bucket")
              .objectName("avatar.jpg")
              .contentType("image/jpeg")
              .visibility(Visibility.PUBLIC)
              .build();

      FileMeta savedFileMeta =
          FileMeta.builder()
              .bucket("test-bucket")
              .objectName("avatar.jpg")
              .contentType("image/jpeg")
              .visibility(Visibility.PUBLIC)
              .status(FileStatus.ACTIVE)
              .build();
      savedFileMeta.setId(UUID.randomUUID());

      when(fileMetaRepository.save(fileMeta)).thenReturn(savedFileMeta);

      // When
      FileMeta result = fileMetaService.save(payload, fileMeta);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(savedFileMeta.getId());
      assertThat(fileMeta.getStatus()).isEqualTo(FileStatus.ACTIVE);
      verify(minioClient).putObject(any(PutObjectArgs.class));
      verify(fileMetaRepository).save(fileMeta);
    }

    @Test
    @DisplayName("should throw NullPointerException when payload is null")
    void save_WhenPayloadIsNull_ShouldThrowNpe() {
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("avatar.jpg").build();
      assertThatThrownBy(() -> fileMetaService.save(null, fileMeta))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Payload cannot be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when fileMeta is null")
    void save_WhenFileMetaIsNull_ShouldThrowNpe() {
      InputStream payload = new ByteArrayInputStream("data".getBytes());
      assertThatThrownBy(() -> fileMetaService.save(payload, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("File metadata cannot be null");
    }

    @Test
    @DisplayName("should throw InvalidInputException when bucket is null")
    void save_WhenBucketIsNull_ShouldThrowInvalidInputException() {
      InputStream payload = new ByteArrayInputStream("data".getBytes());
      FileMeta fileMeta = FileMeta.builder().bucket(null).objectName("avatar.jpg").build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Bucket");
    }

    @Test
    @DisplayName("should throw InvalidInputException when bucket is blank")
    void save_WhenBucketIsBlank_ShouldThrowInvalidInputException() {
      InputStream payload = new ByteArrayInputStream("data".getBytes());
      FileMeta fileMeta = FileMeta.builder().bucket("   ").objectName("avatar.jpg").build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Bucket");
    }

    @Test
    @DisplayName("should throw InvalidInputException when objectName is null")
    void save_WhenObjectNameIsNull_ShouldThrowInvalidInputException() {
      InputStream payload = new ByteArrayInputStream("data".getBytes());
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName(null).build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("ObjectName");
    }

    @Test
    @DisplayName("should throw InvalidInputException when objectName is blank")
    void save_WhenObjectNameIsBlank_ShouldThrowInvalidInputException() {
      InputStream payload = new ByteArrayInputStream("data".getBytes());
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("").build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("ObjectName");
    }

    @Test
    @DisplayName("should throw ServiceException when MinIO putObject fails")
    void save_WhenMinioFails_ShouldThrowServiceException() throws Exception {
      InputStream payload = new ByteArrayInputStream("data".getBytes());
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("avatar.jpg").build();

      doThrow(new RuntimeException("MinIO connection failed"))
          .when(minioClient)
          .putObject(any(PutObjectArgs.class));

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(ServiceException.class);
      verify(fileMetaRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("delete(String bucket, String objectName)")
  class DeleteTest {

    @Test
    @DisplayName("should update status to DELETED when file exists")
    void delete_WhenFileExists_ShouldUpdateStatusToDeleted() {
      // Given
      String bucket = "test-bucket";
      String objectName = "avatar.jpg";
      FileMeta fileMeta =
          FileMeta.builder()
              .bucket(bucket)
              .objectName(objectName)
              .status(FileStatus.ACTIVE)
              .build();

      when(fileMetaRepository.findByBucketAndObjectName(bucket, objectName))
          .thenReturn(Optional.of(fileMeta));

      // When
      fileMetaService.delete(bucket, objectName);

      // Then
      assertThat(fileMeta.getStatus()).isEqualTo(FileStatus.DELETED);
      verify(fileMetaRepository).findByBucketAndObjectName(bucket, objectName);
      verify(fileMetaRepository).save(fileMeta);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when file does not exist")
    void delete_WhenFileDoesNotExist_ShouldThrowResourceNotFoundException() {
      String bucket = "test-bucket";
      String objectName = "missing.jpg";

      when(fileMetaRepository.findByBucketAndObjectName(bucket, objectName))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> fileMetaService.delete(bucket, objectName))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("File metadata");

      verify(fileMetaRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw NullPointerException when bucket is null")
    void delete_WhenBucketIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> fileMetaService.delete(null, "avatar.jpg"))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when objectName is null")
    void delete_WhenObjectNameIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> fileMetaService.delete("test-bucket", null))
          .isInstanceOf(NullPointerException.class);
    }
  }
}
