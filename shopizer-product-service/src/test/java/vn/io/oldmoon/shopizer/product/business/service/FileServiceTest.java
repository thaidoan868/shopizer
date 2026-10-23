package vn.io.oldmoon.shopizer.product.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
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
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Visibility;
import vn.io.oldmoon.shopizer.product.infra.repository.FileRepository;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @Mock private MinioClient minioClient;
  @Mock private FileRepository fileRepository;

  @InjectMocks private FileService fileService;

  @Nested
  @DisplayName("save(InputStream payload, File file)")
  class SaveTests {

    @Test
    @DisplayName("should upload to MinIO and persist file entity when input is valid")
    void save_WhenValidInput_ShouldUploadAndPersist() throws Exception {
      InputStream payload = new ByteArrayInputStream("content".getBytes());
      File file =
          File.builder()
              .bucket("media-public")
              .objectName("image-1.jpg")
              .sizeBytes(7L)
              .contentType("image/jpeg")
              .visibility(Visibility.PUBLIC)
              .status(FileStatus.ACTIVE)
              .build();

      File savedFile =
          File.builder()
              .bucket("media-public")
              .objectName("image-1.jpg")
              .sizeBytes(7L)
              .contentType("image/jpeg")
              .visibility(Visibility.PUBLIC)
              .status(FileStatus.ACTIVE)
              .build();
      savedFile.setId(UUID.randomUUID());

      when(fileRepository.save(file)).thenReturn(savedFile);

      File result = fileService.save(payload, file);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(savedFile.getId());
      verify(minioClient).putObject(any(PutObjectArgs.class));
      verify(fileRepository).save(file);
    }

    @Test
    @DisplayName("should throw NullPointerException when payload is null")
    void save_WhenPayloadIsNull_ShouldThrowNpe() {
      File file = File.builder().bucket("media-public").objectName("image-1.jpg").build();

      assertThatThrownBy(() -> fileService.save(null, file))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Payload must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when file is null")
    void save_WhenFileIsNull_ShouldThrowNpe() {
      InputStream payload = new ByteArrayInputStream("content".getBytes());

      assertThatThrownBy(() -> fileService.save(payload, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("File must not be null");
    }

    @Test
    @DisplayName("should throw InvalidInputException when bucket is null or blank")
    void save_WhenBucketIsInvalid_ShouldThrowInvalidInputException() {
      InputStream payload = new ByteArrayInputStream("content".getBytes());
      File fileWithNullBucket = File.builder().bucket(null).objectName("image-1.jpg").build();
      File fileWithBlankBucket = File.builder().bucket("   ").objectName("image-1.jpg").build();

      assertThatThrownBy(() -> fileService.save(payload, fileWithNullBucket))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Bucket must not be null or blank");

      assertThatThrownBy(() -> fileService.save(payload, fileWithBlankBucket))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("Bucket must not be null or blank");
    }

    @Test
    @DisplayName("should throw InvalidInputException when objectName is null or blank")
    void save_WhenObjectNameIsInvalid_ShouldThrowInvalidInputException() {
      InputStream payload = new ByteArrayInputStream("content".getBytes());
      File fileWithNullObject = File.builder().bucket("media-public").objectName(null).build();
      File fileWithBlankObject = File.builder().bucket("media-public").objectName("  ").build();

      assertThatThrownBy(() -> fileService.save(payload, fileWithNullObject))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("ObjectName must not be null or blank");

      assertThatThrownBy(() -> fileService.save(payload, fileWithBlankObject))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("ObjectName must not be null or blank");
    }

    @Test
    @DisplayName("should throw ServiceException when MinIO putObject fails")
    void save_WhenMinioFails_ShouldThrowServiceException() throws Exception {
      InputStream payload = new ByteArrayInputStream("content".getBytes());
      File file = File.builder().bucket("media-public").objectName("image-1.jpg").build();

      doThrow(new RuntimeException("MinIO error"))
          .when(minioClient)
          .putObject(any(PutObjectArgs.class));

      assertThatThrownBy(() -> fileService.save(payload, file))
          .isInstanceOf(ServiceException.class)
          .hasMessage("Failed to upload file to storage");
      verify(fileRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("download(String bucket, String objectName)")
  class DownloadTests {

    @Test
    @DisplayName("should return InputStream when MinIO getObject succeeds")
    void download_WhenValid_ShouldReturnStream() throws Exception {
      GetObjectResponse mockResponse = mock(GetObjectResponse.class);
      when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

      InputStream result = fileService.download("media-public", "video-1.mp4");

      assertThat(result).isSameAs(mockResponse);
      verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("should throw NullPointerException when bucket or objectName is null")
    void download_WhenNullArgs_ShouldThrowNpe() {
      assertThatThrownBy(() -> fileService.download(null, "video-1.mp4"))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Bucket must not be null");

      assertThatThrownBy(() -> fileService.download("media-public", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("ObjectName must not be null");
    }

    @Test
    @DisplayName("should throw ServiceException when MinIO getObject fails")
    void download_WhenMinioFails_ShouldThrowServiceException() throws Exception {
      when(minioClient.getObject(any(GetObjectArgs.class)))
          .thenThrow(new RuntimeException("MinIO download failure"));

      assertThatThrownBy(() -> fileService.download("media-public", "video-1.mp4"))
          .isInstanceOf(ServiceException.class)
          .hasMessage("Failed to download file from storage");
    }
  }

  @Nested
  @DisplayName("delete(UUID id)")
  class DeleteTests {

    @Test
    @DisplayName("should throw NullPointerException when id is null")
    void delete_WhenIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> fileService.delete(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("ID must not be null");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when file does not exist")
    void delete_WhenFileNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(fileRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> fileService.delete(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("File")
          .hasMessageContaining(id.toString());

      verify(fileRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should remove object from MinIO and hard delete file from repository")
    void delete_WhenFileExists_ShouldRemoveFromMinioAndHardDeleteFromDb() throws Exception {
      UUID id = UUID.randomUUID();
      File file =
          File.builder()
              .bucket("media-public")
              .objectName("image-1.jpg")
              .status(FileStatus.ACTIVE)
              .build();
      file.setId(id);

      when(fileRepository.findById(id)).thenReturn(Optional.of(file));

      fileService.delete(id);

      verify(minioClient).removeObject(any(RemoveObjectArgs.class));
      verify(fileRepository).delete(file);
    }

    @Test
    @DisplayName("should throw ServiceException and not delete DB record when MinIO removeObject fails")
    void delete_WhenMinioRemoveFails_ShouldThrowServiceException() throws Exception {
      UUID id = UUID.randomUUID();
      File file =
          File.builder()
              .bucket("media-public")
              .objectName("image-1.jpg")
              .status(FileStatus.ACTIVE)
              .build();
      file.setId(id);

      when(fileRepository.findById(id)).thenReturn(Optional.of(file));
      doThrow(new RuntimeException("MinIO remove error"))
          .when(minioClient)
          .removeObject(any(RemoveObjectArgs.class));

      assertThatThrownBy(() -> fileService.delete(id))
          .isInstanceOf(ServiceException.class)
          .hasMessage("Failed to delete file from storage");

      verify(fileRepository, never()).delete(any());
    }
  }
}
