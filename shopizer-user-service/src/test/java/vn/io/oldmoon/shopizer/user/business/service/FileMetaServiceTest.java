package vn.io.oldmoon.shopizer.user.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.constant.MediaType;
import vn.io.oldmoon.shopizer.common.core.constant.Visibility;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;
import vn.io.oldmoon.shopizer.user.infra.repository.FileMetaRepository;

@ExtendWith(MockitoExtension.class)
class FileMetaServiceTest {

  @Mock private MinioClient minioClient;
  @Mock private FileMetaRepository fileMetaRepository;

  @InjectMocks private FileMetaService fileMetaService;

  @Nested
  @DisplayName("save(byte[] payload, File fileMeta)")
  class SaveBytesTest {

    @Test
    @DisplayName("should upload to MinIO and save file entity when input is valid")
    void save_WhenValidBytes_ShouldUploadAndPersist() throws Exception {
      // Given
      byte[] payload = "test-image-content".getBytes();
      FileMeta fileMeta =
          FileMeta.builder()
              .bucket("test-bucket")
              .objectName("avatar.jpg")
              .contentType(MediaType.image)
              .visibility(Visibility.PUBLIC)
              .build();

      FileMeta savedFileMeta =
          FileMeta.builder()
              .bucket("test-bucket")
              .objectName("avatar.jpg")
              .sizeBytes((long) payload.length)
              .contentType(MediaType.image)
              .visibility(Visibility.PUBLIC)
              .build();
      savedFileMeta.setId(UUID.randomUUID());

      when(fileMetaRepository.save(fileMeta)).thenReturn(savedFileMeta);

      // When
      FileMeta result = fileMetaService.save(payload, fileMeta);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(savedFileMeta.getId());
      assertThat(fileMeta.getSizeBytes()).isEqualTo(payload.length);
      verify(minioClient).putObject(any(PutObjectArgs.class));
      verify(fileMetaRepository).save(fileMeta);
    }

    @Test
    @DisplayName("should throw NullPointerException when payload is null")
    void save_WhenPayloadIsNull_ShouldThrowNpe() {
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("avatar.jpg").build();
      assertThatThrownBy(() -> fileMetaService.save(null, fileMeta))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw NullPointerException when fileMeta is null")
    void save_WhenFileMetaIsNull_ShouldThrowNpe() {
      byte[] payload = new byte[10];
      assertThatThrownBy(() -> fileMetaService.save(payload, null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw InvalidInputException when bucket is null")
    void save_WhenBucketIsNull_ShouldThrowInvalidInputException() {
      byte[] payload = new byte[10];
      FileMeta fileMeta = FileMeta.builder().bucket(null).objectName("avatar.jpg").build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Bucket");
    }

    @Test
    @DisplayName("should throw InvalidInputException when bucket is blank")
    void save_WhenBucketIsBlank_ShouldThrowInvalidInputException() {
      byte[] payload = new byte[10];
      FileMeta fileMeta = FileMeta.builder().bucket("   ").objectName("avatar.jpg").build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Bucket");
    }

    @Test
    @DisplayName("should throw InvalidInputException when objectName is null")
    void save_WhenObjectNameIsNull_ShouldThrowInvalidInputException() {
      byte[] payload = new byte[10];
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName(null).build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("ObjectName");
    }

    @Test
    @DisplayName("should throw InvalidInputException when objectName is blank")
    void save_WhenObjectNameIsBlank_ShouldThrowInvalidInputException() {
      byte[] payload = new byte[10];
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("").build();

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("ObjectName");
    }

    @Test
    @DisplayName("should throw RuntimeException when MinIO putObject fails")
    void save_WhenMinioFails_ShouldThrowRuntimeException() throws Exception {
      byte[] payload = "data".getBytes();
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("avatar.jpg").build();

      doThrow(new RuntimeException("MinIO error"))
          .when(minioClient)
          .putObject(any(PutObjectArgs.class));

      assertThatThrownBy(() -> fileMetaService.save(payload, fileMeta))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to upload file to storage");
      verify(fileMetaRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("save(InputStream payload, long sizeBytes, File fileMeta)")
  class SaveStreamTest {

    @Test
    @DisplayName("should upload stream and save file entity when input is valid")
    void saveStream_WhenValidInput_ShouldUploadAndPersist() throws Exception {
      InputStream stream = new ByteArrayInputStream("stream-data".getBytes());
      FileMeta fileMeta = FileMeta.builder().bucket("test-bucket").objectName("stream.png").build();
      FileMeta saved = FileMeta.builder().bucket("test-bucket").objectName("stream.png").build();
      saved.setId(UUID.randomUUID());

      when(fileMetaRepository.save(fileMeta)).thenReturn(saved);

      FileMeta result = fileMetaService.save(stream, 11L, fileMeta);

      assertThat(result).isNotNull();
      assertThat(fileMeta.getSizeBytes()).isEqualTo(11L);
      verify(minioClient).putObject(any(PutObjectArgs.class));
      verify(fileMetaRepository).save(fileMeta);
    }
  }
}
