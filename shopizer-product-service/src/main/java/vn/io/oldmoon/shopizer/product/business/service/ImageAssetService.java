package vn.io.oldmoon.shopizer.product.business.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  private final ImageAssetRepository imageAssetRepository;

  @Value("${minio.bucket.media-public:media-public}")
  private String mediaBucket;

  /**
   * Uploads multiple images, generating three variations (thumbnail 200px, medium 800px, large
   * 1600px max edge with EXIF stripped), uploading all four files to MinIO, saving each File record
   * and persisting an ImageAsset grouping them.
   *
   * @param files the list of uploaded image files
   * @return the list of persisted ImageAsset entities
   */
  @Transactional
  public List<ImageAsset> uploadImages(List<MultipartFile> files) {
    Objects.requireNonNull(files, "Files list must not be null");
    if (files.isEmpty()) {
      throw new InvalidInputException("Files list must not be empty");
    }

    List<ImageAsset> results = new ArrayList<>();
    for (MultipartFile file : files) {
      ImageUtil.validateContent(file);

      String uniqueId = UUID.randomUUID().toString();
      String extracted = FileUtil.extractFilename(file.getOriginalFilename());
      String originalFilename =
          (extracted != null && !extracted.isBlank()) ? extracted : "image.jpg";

      String originalObjectName = "image-original-" + uniqueId + "-" + originalFilename;
      String thumbnailObjectName = "image-thumbnail-" + uniqueId + ".jpg";
      String mediumObjectName = "image-medium-" + uniqueId + ".jpg";
      String largeObjectName = "image-large-" + uniqueId + ".jpg";

      String contentType = file.getContentType();
      if (contentType == null || contentType.isBlank()) {
        contentType = JPEG_CONTENT_TYPE;
      }

      byte[] thumbnailBytes = resizeVariation(file, THUMBNAIL_MAX_EDGE, originalFilename);
      byte[] mediumBytes = resizeVariation(file, MEDIUM_MAX_EDGE, originalFilename);
      byte[] largeBytes = resizeVariation(file, LARGE_MAX_EDGE, originalFilename);

      File savedOriginal;
      try (InputStream originalStream = file.getInputStream()) {
        File originalEntity =
            File.builder()
                .bucket(mediaBucket)
                .objectName(originalObjectName)
                .sizeBytes(file.getSize())
                .contentType(contentType)
                .visibility(Visibility.PUBLIC)
                .status(FileStatus.ACTIVE)
                .build();
        savedOriginal = fileService.save(originalStream, originalEntity);
      } catch (IOException e) {
        log.error("Failed to read file stream for: {}", originalFilename, e);
        throw new ServiceException("Failed to read file for upload", e);
      }

      File savedThumbnail =
          saveVariation(thumbnailObjectName, thumbnailBytes);
      File savedMedium =
          saveVariation(mediumObjectName, mediumBytes);
      File savedLarge =
          saveVariation(largeObjectName, largeBytes);

      ImageAsset imageAsset =
          ImageAsset.builder()
              .original(savedOriginal)
              .thumbnail(savedThumbnail)
              .medium(savedMedium)
              .large(savedLarge)
              .build();
      ImageAsset savedAsset = imageAssetRepository.save(imageAsset);

      log.info(
          "Successfully uploaded and persisted ImageAsset: id={}, originalId={}, thumbnailId={}, mediumId={}, largeId={}",
          savedAsset.getId(),
          savedOriginal.getId(),
          savedThumbnail.getId(),
          savedMedium.getId(),
          savedLarge.getId());

      results.add(savedAsset);
    }

    return results;
  }

  /**
   * Deletes an ImageAsset by soft-deleting all four associated File records.
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

    fileService.delete(imageAsset.getOriginal().getId());
    fileService.delete(imageAsset.getThumbnail().getId());
    fileService.delete(imageAsset.getMedium().getId());
    fileService.delete(imageAsset.getLarge().getId());

    log.info("Soft deleted files for ImageAsset id={}", imageAsset.getId());
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

  private File saveVariation(String objectName, byte[] bytes) {
    File variationEntity =
        File.builder()
            .bucket(mediaBucket)
            .objectName(objectName)
            .sizeBytes((long) bytes.length)
            .contentType(JPEG_CONTENT_TYPE)
            .visibility(Visibility.PUBLIC)
            .status(FileStatus.ACTIVE)
            .build();
    return fileService.save(new ByteArrayInputStream(bytes), variationEntity);
  }
}
