package vn.io.oldmoon.shopizer.product.business.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.common.core.utility.FileUtil;
import vn.io.oldmoon.shopizer.common.core.utility.ImageUtil;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.Visibility;
import vn.io.oldmoon.shopizer.product.infra.repository.FileRepository;
import vn.io.oldmoon.shopizer.product.infra.repository.ImageAssetRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageAssetService {

  private static final int THUMBNAIL_MAX_EDGE = 200;
  private static final int MEDIUM_MAX_EDGE = 800;
  private static final int LARGE_MAX_EDGE = 1600;
  private static final String JPEG_CONTENT_TYPE = "image/jpeg";

  private final FileService fileService;
  private final FileRepository fileRepository;
  private final ImageAssetRepository imageAssetRepository;
  private final TransactionTemplate transactionTemplate;

  @Value("${minio.bucket.media-public:media-public}")
  private String mediaBucket;

  /**
   * Uploads an image using a 3-phase execution model:
   *
   * @param file the uploaded image file
   * @return the persisted ImageAsset entity
   */
  public ImageAsset uploadImage(MultipartFile file) {
    Objects.requireNonNull(file, "File must not be null");

    // --- Phase 1: Process & Resize in memory (Fail-Fast before I/O) ---
    ImageUtil.validateContent(file);

    String uniqueId = UUID.randomUUID().toString();
    String extracted = FileUtil.extractFilename(file.getOriginalFilename());
    String originalFilename = (extracted != null && !extracted.isBlank()) ? extracted : "image.jpg";
    String contentType =
        (file.getContentType() != null && !file.getContentType().isBlank())
            ? file.getContentType()
            : JPEG_CONTENT_TYPE;

    byte[] thumbnailBytes = resizeVariation(file, THUMBNAIL_MAX_EDGE, originalFilename);
    byte[] mediumBytes = resizeVariation(file, MEDIUM_MAX_EDGE, originalFilename);
    byte[] largeBytes = resizeVariation(file, LARGE_MAX_EDGE, originalFilename);

    // --- Phase 2: Upload files to MinIO with DELETED status (No DB transaction held) ---
    List<File> uploadedFiles;
    ImageAsset assetToPersist;

    String originalObjectName = "image-original-" + uniqueId + "-" + originalFilename;
    String thumbnailObjectName = "image-thumbnail-" + uniqueId + ".jpg";
    String mediumObjectName = "image-medium-" + uniqueId + ".jpg";
    String largeObjectName = "image-large-" + uniqueId + ".jpg";

    File savedOriginal = uploadOriginal(file, originalObjectName, contentType, originalFilename);
    File savedThumbnail = saveVariation(thumbnailObjectName, thumbnailBytes);
    File savedMedium = saveVariation(mediumObjectName, mediumBytes);
    File savedLarge = saveVariation(largeObjectName, largeBytes);

    uploadedFiles = List.of(savedOriginal, savedThumbnail, savedMedium, savedLarge);
    assetToPersist =
        ImageAsset.builder()
            .original(savedOriginal)
            .thumbnail(savedThumbnail)
            .medium(savedMedium)
            .large(savedLarge)
            .build();

    // --- Phase 3: Atomic DB Commit & Status Flip ---
    return transactionTemplate.execute(
        status -> {
          ImageAsset savedAsset = imageAssetRepository.save(assetToPersist);

          List<UUID> uploadedFileIds = uploadedFiles.stream().map(File::getId).toList();
          uploadedFiles.forEach(f -> f.setStatus(FileStatus.ACTIVE));
          fileRepository.updateStatusByIds(uploadedFileIds, FileStatus.ACTIVE);

          log.info(
              "Successfully persisted ImageAsset id={} and activated {} files",
              savedAsset.getId(),
              uploadedFiles.size());
          return savedAsset;
        });
  }

  /**
   * Retrieves an ImageAsset by its UUID.
   *
   * @param assetId the UUID of the ImageAsset
   * @return the ImageAsset entity
   * @throws ResourceNotFoundException if not found
   */
  @Transactional(readOnly = true)
  public ImageAsset get(UUID assetId) {
    Objects.requireNonNull(assetId, "Asset ID must not be null");
    return imageAssetRepository
        .findById(assetId)
        .orElseThrow(() -> new ResourceNotFoundException("ImageAsset", assetId.toString()));
  }

  /**
   * Hard deletes an ImageAsset and deletes all four associated File records.
   *
   * @param id the UUID of the ImageAsset
   */
  @Transactional
  public void delete(UUID id) {
    Objects.requireNonNull(id, "ID must not be null");
    log.info("Deleting image asset with id='{}'", id);

    ImageAsset imageAsset =
        imageAssetRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ImageAsset", id.toString()));

    UUID originalId = imageAsset.getOriginal() != null ? imageAsset.getOriginal().getId() : null;
    UUID thumbnailId =
        imageAsset.getThumbnail() != null ? imageAsset.getThumbnail().getId() : null;
    UUID mediumId = imageAsset.getMedium() != null ? imageAsset.getMedium().getId() : null;
    UUID largeId = imageAsset.getLarge() != null ? imageAsset.getLarge().getId() : null;

    imageAssetRepository.delete(imageAsset);

    if (originalId != null) {
      fileService.delete(originalId);
    }
    if (thumbnailId != null) {
      fileService.delete(thumbnailId);
    }
    if (mediumId != null) {
      fileService.delete(mediumId);
    }
    if (largeId != null) {
      fileService.delete(largeId);
    }

    log.info("Hard deleted ImageAsset id={} and its associated files", id);
  }

  private File uploadOriginal(
      MultipartFile file, String objectName, String contentType, String originalFilename) {
    try (InputStream originalStream = file.getInputStream()) {
      File originalEntity =
          File.builder()
              .bucket(mediaBucket)
              .objectName(objectName)
              .sizeBytes(file.getSize())
              .contentType(contentType)
              .visibility(Visibility.PUBLIC)
              .status(FileStatus.DELETED) // Saved as DELETED until Phase 3 commit
              .build();
      return fileService.save(originalStream, originalEntity);
    } catch (IOException e) {
      log.error("Failed to read file stream for: {}", originalFilename, e);
      throw new ServiceException("Failed to read file for upload", e);
    }
  }

  private File saveVariation(String objectName, byte[] bytes) {
    File variationEntity =
        File.builder()
            .bucket(mediaBucket)
            .objectName(objectName)
            .sizeBytes((long) bytes.length)
            .contentType(JPEG_CONTENT_TYPE)
            .visibility(Visibility.PUBLIC)
            .status(FileStatus.DELETED) // Saved as DELETED until Phase 3 commit
            .build();
    return fileService.save(new ByteArrayInputStream(bytes), variationEntity);
  }
  private byte[] resizeVariation(MultipartFile file, int maxEdge, String originalFilename) {
    try (InputStream in = file.getInputStream();
         InputStream resizedStream = ImageUtil.resizeToMaxEdge(in, maxEdge)) {
      return resizedStream.readAllBytes();
    } catch (IOException e) {
      log.error("Failed to process image variation (maxEdge={}): {}", maxEdge, originalFilename, e);
      throw new InvalidInputException("Failed to process image: " + e.getMessage());
    }
  }
}
