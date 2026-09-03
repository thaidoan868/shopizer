package vn.io.oldmoon.shopizer.user.business.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.user.infra.data.constant.FileStatus;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;
import vn.io.oldmoon.shopizer.user.infra.repository.FileMetaRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileMetaService {

  private final MinioClient minioClient;
  private final FileMetaRepository fileMetaRepository;

  /**
   * Uploads stream payload to MinIO and persists file metadata to the database.
   *
   * @param payload the file content stream
   * @param fileMeta the file metadata entity
   * @return the persisted File entity
   */
  @Transactional
  public FileMeta save(InputStream payload, FileMeta fileMeta) {
    Objects.requireNonNull(payload, "Payload cannot be null");
    Objects.requireNonNull(fileMeta, "File metadata cannot be null");

    if (fileMeta.getBucket() == null || fileMeta.getBucket().isBlank()) {
      throw new InvalidInputException("Bucket cannot be null or blank");
    }
    if (fileMeta.getObjectName() == null || fileMeta.getObjectName().isBlank()) {
      throw new InvalidInputException("ObjectName cannot be null or blank");
    }

    try {
      long size =
          fileMeta.getSizeBytes() != null && fileMeta.getSizeBytes() >= 0
              ? fileMeta.getSizeBytes()
              : -1L;
      long partSize = size < 0 ? 10485760L : -1L;
      String rawContentType = fileMeta.getContentType();
      String contentType;
      if (rawContentType == null || rawContentType.isBlank()) {
        contentType = "application/octet-stream";
      } else if (!rawContentType.contains("/")) {
        contentType =
            switch (rawContentType.toLowerCase()) {
              case "image" -> "image/jpeg";
              case "video" -> "video/mp4";
              case "audio" -> "audio/mpeg";
              default -> "application/octet-stream";
            };
      } else {
        contentType = rawContentType;
      }
      PutObjectArgs.Builder builder =
          PutObjectArgs.builder()
              .bucket(fileMeta.getBucket())
              .object(fileMeta.getObjectName())
              .contentType(contentType)
              .stream(payload, size, partSize);
      minioClient.putObject(builder.build());
      log.info(
          "File uploaded to MinIO: bucket={}, objectName={}",
          fileMeta.getBucket(),
          fileMeta.getObjectName());
    } catch (Exception e) {
      log.error(
          "Failed to upload file to MinIO: bucket={}, objectName={}",
          fileMeta.getBucket(),
          fileMeta.getObjectName());
      throw new ServiceException("Failed to upload file to storage", e);
    }

    if (fileMeta.getStatus() == null) {
      fileMeta.setStatus(FileStatus.ACTIVE);
    }

    FileMeta saved = fileMetaRepository.save(fileMeta);
    log.info(
        "Persisting file metadata id={}, bucket={}, objectName={}",
        saved.getId(),
        saved.getBucket(),
        saved.getObjectName());
    return saved;
  }

  /**
   * Marks a file as deleted in the database. The actual file in MinIO is not deleted.
   *
   * @param bucket the bucket name
   * @param objectName the object name
   * @throws ResourceNotFoundException if the file metadata does not exist
   */
  @Transactional
  public void delete(String bucket, String objectName) {
    Objects.requireNonNull(bucket);
    Objects.requireNonNull(objectName);

    FileMeta fileMeta =
        fileMetaRepository
            .findByBucketAndObjectName(bucket, objectName)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "File metadata", "bucket=%s, objectName=%s".formatted(bucket, objectName)));
    fileMeta.setStatus(FileStatus.DELETED);
    fileMetaRepository.save(fileMeta);
    log.info(
        "Soft deleted file metadata id={}, bucket={}, objectName={}",
        fileMeta.getId(),
        fileMeta.getBucket(),
        fileMeta.getObjectName());
  }
}
