package vn.io.oldmoon.shopizer.product.business.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.repository.FileRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

  private final MinioClient minioClient;
  private final FileRepository fileRepository;

  /**
   * Uploads a stream payload to MinIO and persists the file metadata record in the database.
   *
   * @param payload the file content stream
   * @param file the file metadata entity to persist
   * @return the persisted File entity
   */
  public File save(InputStream payload, File file) {
    Objects.requireNonNull(payload, "Payload must not be null");
    Objects.requireNonNull(file, "File must not be null");

    if (file.getBucket() == null || file.getBucket().isBlank()) {
      throw new InvalidInputException("Bucket must not be null or blank");
    }
    if (file.getObjectName() == null || file.getObjectName().isBlank()) {
      throw new InvalidInputException("ObjectName must not be null or blank");
    }

    long size = file.getSizeBytes() != null && file.getSizeBytes() >= 0 ? file.getSizeBytes() : -1L;
    long partSize = size < 0 ? 10485760L : -1L;
    String contentType =
        (file.getContentType() != null && !file.getContentType().isBlank())
            ? file.getContentType()
            : "application/octet-stream";

    try {
      PutObjectArgs args =
          PutObjectArgs.builder()
              .bucket(file.getBucket())
              .object(file.getObjectName())
              .contentType(contentType)
              .stream(payload, size, partSize)
              .build();
      minioClient.putObject(args);
      log.info(
          "File uploaded to MinIO: bucket={}, objectName={}",
          file.getBucket(),
          file.getObjectName());
    } catch (Exception e) {
      log.error(
          "Failed to upload file to MinIO: bucket={}, objectName={}",
          file.getBucket(),
          file.getObjectName(),
          e);
      throw new ServiceException("Failed to upload file to storage", e);
    }

    File saved = fileRepository.save(file);
    log.info(
        "Saved file metadata id={}, bucket={}, objectName={}",
        saved.getId(),
        saved.getBucket(),
        saved.getObjectName());
    return saved;
  }

  /**
   * Downloads a file stream from MinIO storage.
   *
   * @param bucket the bucket name
   * @param objectName the object name in MinIO
   * @return the InputStream of the stored object
   */
  public InputStream download(String bucket, String objectName) {
    Objects.requireNonNull(bucket, "Bucket must not be null");
    Objects.requireNonNull(objectName, "ObjectName must not be null");

    try {
      GetObjectArgs args = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
      InputStream stream = minioClient.getObject(args);
      log.debug(
          "Created a connection to download file from MinIO: bucket={}, objectName={}",
          bucket,
          objectName);
      return stream;
    } catch (Exception e) {
      log.error(
          "Failed to download file from MinIO: bucket={}, objectName={}", bucket, objectName, e);
      throw new ServiceException("Failed to download file from storage", e);
    }
  }

  /**
   * Hard deletes a file metadata record from the database and removes the object from MinIO
   * storage.
   *
   * @param id the UUID of the file to delete
   */
  @Transactional
  public void delete(UUID id) {
    Objects.requireNonNull(id, "ID must not be null");
    log.info("Deleting file with id='{}'", id);
    File file =
        fileRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("File", id.toString()));

    try {
      RemoveObjectArgs args =
          RemoveObjectArgs.builder().bucket(file.getBucket()).object(file.getObjectName()).build();
      minioClient.removeObject(args);
      log.info(
          "File deleted from MinIO: bucket={}, objectName={}",
          file.getBucket(),
          file.getObjectName());
    } catch (Exception e) {
      log.error(
          "Failed to delete file from MinIO: bucket={}, objectName={}",
          file.getBucket(),
          file.getObjectName(),
          e);
      throw new ServiceException("Failed to delete file from storage", e);
    }

    fileRepository.delete(file);
    log.info(
        "Hard deleted file metadata id={}, bucket={}, objectName={}",
        file.getId(),
        file.getBucket(),
        file.getObjectName());
  }
}
