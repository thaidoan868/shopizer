package vn.io.oldmoon.shopizer.product.business.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.event.EventPublisher;
import vn.io.oldmoon.shopizer.common.core.event.RabbitMqEvent;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.common.core.utility.FileUtil;
import vn.io.oldmoon.shopizer.common.core.utility.VideoUtil;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.product.business.event.video.VideoProcessingEvent;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Video;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoQuality;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Visibility;
import vn.io.oldmoon.shopizer.product.infra.repository.FileRepository;
import vn.io.oldmoon.shopizer.product.infra.repository.VideoAssetRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoAssetService {

  private static final String MP4_CONTENT_TYPE = "video/mp4";

  private final FileService fileService;
  private final FileRepository fileRepository;
  private final VideoAssetRepository videoAssetRepository;
  private final EventPublisher eventPublisher;
  private final TransactionTemplate transactionTemplate;

  @Value("${minio.bucket.media-public:media-public}")
  private String mediaBucket;

  /**
   * @param file the uploaded video file
   * @return the initialized VideoAsset entity
   */
  public VideoAsset uploadVideo(MultipartFile file) {
    Objects.requireNonNull(file, "File must not be null");

    // --- Step 1: Validate & probe duration (In-memory, fail-fast) ---
    VideoUtil.validateContent(file);
    double duration = VideoUtil.extractDuration(file);

    String uniqueId = UUID.randomUUID().toString();
    String extracted = FileUtil.extractFilename(file.getOriginalFilename());
    String originalFilename = (extracted != null && !extracted.isBlank()) ? extracted : "video.mp4";
    String originalObjectName = "video-original-" + uniqueId + "-" + originalFilename;

    String contentType = file.getContentType();
    if (contentType == null || contentType.isBlank()) {
      contentType = MP4_CONTENT_TYPE;
    }

    // --- Step 2: Upload file to MinIO (Slow I/O, NO DB transaction held) ---
    File savedOriginal = uploadOriginal(file, originalObjectName, contentType, originalFilename);

    // --- Step 3: Atomic DB Commit & Event Dispatch
    return transactionTemplate.execute(
        status -> {
          savedOriginal.setStatus(FileStatus.ACTIVE);
          Video notAvailable =
              Video.builder().status(VideoStatus.NOT_AVAILABLE).duration(null).file(null).build();
          VideoAsset videoAsset =
              VideoAsset.builder()
                  .original(
                      Video.builder()
                          .status(VideoStatus.AVAILABLE)
                          .duration(duration)
                          .file(savedOriginal)
                          .build())
                  .p360(notAvailable)
                  .p720(notAvailable)
                  .p1080(notAvailable)
                  .build();
          log.info(
              "Persisting new VideoAsset with original file: bucket={}, objectName={}",
              savedOriginal.getBucket(),
              savedOriginal.getObjectName());
          VideoAsset savedAsset = videoAssetRepository.save(videoAsset);

          fileRepository.save(savedOriginal);

          for (VideoQuality quality : VideoQuality.values()) {
            VideoProcessingEvent event =
                new VideoProcessingEvent(savedAsset.getId(), savedOriginal, quality);
            eventPublisher.publish(
                new RabbitMqEvent(event, RabbitMqConfig.videoProcessingBindingKey));
          }
          return savedAsset;
        });
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
              .status(FileStatus.DELETED)
              .build();
      return fileService.save(originalStream, originalEntity);
    } catch (IOException e) {
      log.error("Failed to read video file stream for: {}", originalFilename, e);
      throw new ServiceException("Failed to read video file for upload", e);
    }
  }

  /**
   * Uploads a transcoded video variation file to MinIO, persists its File metadata initially as
   * DELETED, and atomically commits both the variation attachment and ACTIVE status flip.
   *
   * @param assetId the UUID of the VideoAsset
   * @param quality the transcoded resolution quality
   * @param outputTempFile the local path to the transcoded MP4 file
   */
  public void saveVideoVariation(UUID assetId, VideoQuality quality, Path outputTempFile)
      throws IOException {
    Objects.requireNonNull(assetId, "Asset ID must not be null");
    Objects.requireNonNull(quality, "Video quality must not be null");
    Objects.requireNonNull(outputTempFile, "Output temp file must not be null");

    // --- Phase 1: Validate & probe local file metrics (Fail-Fast before I/O) ---
    long outputSize = Files.size(outputTempFile);
    double variationDuration = VideoUtil.extractDuration(outputTempFile);
    String variationObjectName =
        "video-" + quality.getLabel() + "-" + assetId + "-" + UUID.randomUUID() + ".mp4";
    String contentType = Files.probeContentType(outputTempFile);
    if (contentType == null || contentType.isBlank()) {
      contentType = MP4_CONTENT_TYPE;
    }

    // --- Phase 2: Remote MinIO Upload with DELETED status (No DB transaction held) ---
    File savedVariationFile;
    try (InputStream outputStream = Files.newInputStream(outputTempFile)) {
      File variationFileEntity =
          File.builder()
              .bucket(mediaBucket)
              .objectName(variationObjectName)
              .sizeBytes(outputSize)
              .contentType(contentType)
              .visibility(Visibility.PUBLIC)
              .status(FileStatus.DELETED) // Safe against crashes before DB commit
              .build();
      savedVariationFile = fileService.save(outputStream, variationFileEntity);
    }

    // --- Phase 3: Atomic DB Commit & Status Flip
    transactionTemplate.executeWithoutResult(
        status -> {
          VideoAsset videoAsset = this.get(assetId);
          savedVariationFile.setStatus(FileStatus.ACTIVE);

          Video completedVideo =
              Video.builder()
                  .status(VideoStatus.AVAILABLE)
                  .duration(variationDuration)
                  .file(savedVariationFile)
                  .build();

          updateVariationOnAsset(videoAsset, quality, completedVideo);
          log.info(
              "Saving video variation: assetId={}, quality={}, fileId={}",
              assetId,
              quality.getLabel(),
              savedVariationFile.getId());
          fileRepository.save(savedVariationFile);
        });
  }

  /**
   * Retrieves a VideoAsset by its UUID.
   *
   * @param assetId the UUID of the VideoAsset
   * @return the VideoAsset entity
   * @throws ResourceNotFoundException if not found
   */
  @Transactional(readOnly = true)
  public VideoAsset get(UUID assetId) {
    Objects.requireNonNull(assetId, "Asset ID must not be null");
    return videoAssetRepository
        .findById(assetId)
        .orElseThrow(() -> new ResourceNotFoundException("VideoAsset", assetId.toString()));
  }

  /**
   * Hard deletes a VideoAsset and deletes all associated File records.
   *
   * @param id the UUID of the VideoAsset
   */
  @Transactional
  public void delete(UUID id) {
    Objects.requireNonNull(id, "ID must not be null");

    VideoAsset videoAsset = this.get(id);

    deleteVideoFile(videoAsset.getOriginal());
    deleteVideoFile(videoAsset.getP360());
    deleteVideoFile(videoAsset.getP720());
    deleteVideoFile(videoAsset.getP1080());

    videoAssetRepository.delete(videoAsset);
    log.info("Hard deleting VideoAsset id={} and its associated files", id);
  }

  private void deleteVideoFile(Video video) {
    if (video != null && video.getFile() != null && video.getFile().getId() != null) {
      fileService.delete(video.getFile().getId());
    }
  }

  /**
   * Transcodes an input video file to the specified quality using FFmpeg.
   *
   * @param inputPath the local input video file path
   * @param outputPath the local output MP4 file path
   * @param quality the target video quality
   */
  public void transcodeWithFfmpeg(Path inputPath, Path outputPath, VideoQuality quality)
      throws IOException, InterruptedException {

    Objects.requireNonNull(inputPath, "Input path must not be null");
    Objects.requireNonNull(outputPath, "Output path must not be null");
    Objects.requireNonNull(quality, "Video quality must not be null");

    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "ffmpeg",
            "-y", // Overwrite output without asking
            "-i",
            inputPath.toAbsolutePath().toString(),
            "-vf",
            "scale=-2:" + quality.getHeight(),
            "-c:v",
            "libx264",
            "-pix_fmt",
            "yuv420p", // Broad browser/device compatibility
            "-b:v",
            quality.getVideoBitrate(),
            "-preset",
            "medium",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            outputPath.toAbsolutePath().toString());

    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    StringBuilder outputBuilder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        outputBuilder.append(line).append(System.lineSeparator());
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new ServiceException("FFmpeg failed:\n" + outputBuilder);
    }
    log.debug(
        "FFmpeg transcoding completed: input={}, output={}, quality={}, FFmpegOutput={}",
        inputPath,
        outputPath,
        quality.getLabel(),
        outputBuilder);
  }

  public void updateVariationOnAsset(VideoAsset videoAsset, VideoQuality quality, Video variation) {
    Objects.requireNonNull(videoAsset, "Video asset must not be null");
    Objects.requireNonNull(videoAsset.getId(), "Video asset ID must not be null");
    Objects.requireNonNull(quality, "Video quality must not be null");
    Objects.requireNonNull(variation, "Video variation must not be null");

    switch (quality) {
      case P360 -> videoAsset.setP360(variation);
      case P720 -> videoAsset.setP720(variation);
      case P1080 -> videoAsset.setP1080(variation);
    }
    videoAssetRepository.save(videoAsset);
  }
}
