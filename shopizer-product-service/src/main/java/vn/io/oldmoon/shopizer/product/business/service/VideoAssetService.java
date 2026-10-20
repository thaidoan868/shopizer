package vn.io.oldmoon.shopizer.product.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import vn.io.oldmoon.shopizer.common.core.event.EventPublisher;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
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
import vn.io.oldmoon.shopizer.product.infra.repository.VideoAssetRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoAssetService {

  private static final String MP4_CONTENT_TYPE = "video/mp4";

  private final FileService fileService;
  private final VideoAssetRepository videoAssetRepository;
  private final EventPublisher eventPublisher;

  @Value("${minio.bucket.media-public:media-public}")
  private String mediaBucket;

  /**
   * Uploads original videos to MinIO, persists File metadata, initializes VideoAsset records with
   * PROCESSING variations, and publishes RabbitMQ events for 360p, 720p, and 1080p transcoding.
   *
   * @param files the list of uploaded video files
   * @return the list of initialized VideoAsset entities
   */
  @Transactional
  public List<VideoAsset> uploadVideos(List<MultipartFile> files) {
    Objects.requireNonNull(files, "Files list must not be null");
    if (files.isEmpty()) {
      throw new InvalidInputException("Files list must not be empty");
    }

    List<VideoAsset> results = new ArrayList<>();
    for (MultipartFile file : files) {
      VideoUtil.validateContent(file);

      String uniqueId = UUID.randomUUID().toString();
      String extracted = FileUtil.extractFilename(file.getOriginalFilename());
      String originalFilename =
          (extracted != null && !extracted.isBlank()) ? extracted : "video.mp4";
      String originalObjectName = "video-original-" + uniqueId + "-" + originalFilename;

      String contentType = file.getContentType();
      if (contentType == null || contentType.isBlank()) {
        contentType = MP4_CONTENT_TYPE;
      }

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
        log.error("Failed to read video file stream for: {}", originalFilename, e);
        throw new ServiceException("Failed to read video file for upload", e);
      }

      VideoAsset videoAsset =
          VideoAsset.builder()
              .original(Video.builder().status(VideoStatus.AVAILABLE).file(savedOriginal).build())
              .p360(Video.builder().status(VideoStatus.PROCESSING).file(null).build())
              .p720(Video.builder().status(VideoStatus.PROCESSING).file(null).build())
              .p1080(Video.builder().status(VideoStatus.PROCESSING).file(null).build())
              .build();
      VideoAsset savedAsset = videoAssetRepository.save(videoAsset);

      log.info(
          "Initialized VideoAsset id={}, originalFileId={}",
          savedAsset.getId(),
          savedOriginal.getId());

      for (VideoQuality quality : VideoQuality.values()) {
        VideoProcessingEvent event =
            new VideoProcessingEvent(savedAsset.getId(), savedOriginal, quality);
        eventPublisher.publish(event, RabbitMqConfig.videoProcessingBindingKey);
      }

      results.add(savedAsset);
    }

    return results;
  }

  // don't put @transactional here, ensure update videoasset is the last step
  public void saveVideoVariation() {
    long outputSize = Files.size(outputTempFile);
    String variationObjectName =
        "video-" + quality.getLabel() + "-" + assetId + "-" + UUID.randomUUID() + ".mp4";

    File savedVariationFile;
    try (InputStream outputStream = Files.newInputStream(outputTempFile)) {
      File variationFileEntity =
          File.builder()
              .bucket(sourceFile.getBucket())
              .objectName(variationObjectName)
              .sizeBytes(outputSize)
              .contentType(MP4_CONTENT_TYPE)
              .visibility(
                  sourceFile.getVisibility() != null
                      ? sourceFile.getVisibility()
                      : Visibility.PUBLIC)
              .status(FileStatus.ACTIVE)
              .build();
      savedVariationFile = fileService.save(outputStream, variationFileEntity);
    }

    Video completedVideo =
        Video.builder().status(VideoStatus.AVAILABLE).file(savedVariationFile).build();
    updateVariationOnAsset(videoAsset, quality, completedVideo);
    videoAssetRepository.save(videoAsset);

    log.info(
        "Successfully processed video variation: assetId={}, quality={}, fileId={}",
        assetId,
        quality.getLabel(),
        savedVariationFile.getId());
  }

  /**
   * Soft deletes a VideoAsset by soft-deleting all associated File records and marking variations
   * as NOT_AVAILABLE.
   *
   * @param id the UUID of the VideoAsset
   */
  @Transactional
  public void delete(UUID id) {
    Objects.requireNonNull(id, "ID must not be null");
    log.info("Deleting video asset with id='{}'", id);

    VideoAsset videoAsset =
        videoAssetRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("VideoAsset", id.toString()));

    softDeleteVideoVariation(videoAsset.getOriginal());
    softDeleteVideoVariation(videoAsset.getP360());
    softDeleteVideoVariation(videoAsset.getP720());
    softDeleteVideoVariation(videoAsset.getP1080());

    videoAssetRepository.save(videoAsset);
    log.info("Soft deleted files for VideoAsset id={}", videoAsset.getId());
  }

  private void transcodeWithFfmpeg(
      Path inputPath,
      Path outputPath,
      VideoQuality quality,
      double totalDurationSeconds,
      java.util.function.Consumer<Double> progressCallback)
      throws IOException, InterruptedException {

    // add not null checks here

    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-i",
            inputPath.toAbsolutePath().toString(),
            "-progress",
            "pipe:1",
            "-nostats",
            "-vf",
            "scale=-2:" + quality.getHeight(),
            "-c:v",
            "libx264",
            "-b:v",
            quality.getVideoBitrate(),
            "-preset",
            "fast",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            outputPath.toAbsolutePath().toString());

    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    StringBuilder outputBuilder = new StringBuilder();
    long currentMicroseconds = 0;
    double speed = 1.0;
    long lastRedisUpdateTime = System.currentTimeMillis();

    try (java.io.BufferedReader reader =
        new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

      String line;
      while ((line = reader.readLine()) != null) {
        outputBuilder.append(line).append(System.lineSeparator());

        if (line.startsWith("out_time_us=")) {
          try {
            currentMicroseconds = Long.parseLong(line.substring(12).trim());
          } catch (NumberFormatException e) {
            log.warn("Failed to parse out_time_us from ffmpeg output: {}", line, e);
          }
        } else if (line.startsWith("speed=")) {
          String speedStr = line.substring(6).trim(); // e.g. "1.5x"
          if (speedStr.endsWith("x") && !speedStr.equals("N/Ax") && !speedStr.equals("0x")) {
            try {
              speed = Double.parseDouble(speedStr.substring(0, speedStr.length() - 1));
            } catch (NumberFormatException e) {
              log.warn("Failed to parse speed from ffmpeg output: {}", line, e);
            }
          }
        } else if (line.startsWith("progress=") && totalDurationSeconds > 0) {

          // Throttle Updates: Only trigger callback at most once every 1 second
          long now = System.currentTimeMillis();
          if (now - lastRedisUpdateTime >= 1000) {
            double currentSeconds = currentMicroseconds / 1_000_000.0;
            double percentage = Math.min(100.0, (currentSeconds / totalDurationSeconds) * 100.0);
            double remainingSeconds = Math.max(0, totalDurationSeconds - currentSeconds);
            double timeLeftSeconds = speed > 0 ? remainingSeconds / speed : remainingSeconds;

            progressCallback.accept(
                new VideoProgressDto(
                    Math.round(percentage * 100.0) / 100.0, // format to 2 decimal places
                    speed,
                    Math.round(timeLeftSeconds),
                    "PROCESSING"));

            lastRedisUpdateTime = now;
          }
        }
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new ServiceException("FFmpeg failed:\n" + outputBuilder);
    }
  }

  private void saveProgressToRedis(String key, VideoProgressDto progress) {
    try {
      String json = objectMapper.writeValueAsString(progress);
      // Save to Redis with a 24-hour TTL so old data cleans itself up automatically
      redisTemplate.opsForValue().set(key, json, Duration.ofHours(24));
    } catch (Exception e) {
      log.warn("Failed to save progress to Redis for key: {}", key, e);
    }
  }

  private void softDeleteVideoVariation(Video video) {
    if (video == null) {
      return;
    }
    video.setStatus(VideoStatus.NOT_AVAILABLE);
    if (video.getFile() != null && video.getFile().getId() != null) {
      fileService.delete(video.getFile().getId());
      video.getFile().setStatus(FileStatus.DELETED);
    }
  }

  private void deleteTempFileQuietly(Path path) {
    if (path != null) {
      try {
        Files.deleteIfExists(path);
      } catch (IOException e) {
        log.warn("Failed to delete temporary file: {}", path, e);
      }
    }
  }
}
