package vn.io.oldmoon.shopizer.product.business.event.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.product.business.service.VideoAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingEventListener
    implements ApplicationEventListener<VideoProcessingEvent> {

  private final VideoAssetService videoAssetService;

  @Override
  @RabbitListener(
      queues = RabbitMqConfig.videoProcessingQueue,
      containerFactory = "videoRabbitListenerContainerFactory")
  public void handle(VideoProcessingEvent event) {
    log.info(
        "Received VideoProcessingEvent for assetId={}, quality={}",
        event.assetId(),
        event.targetQuality());
    // validation
    Objects.requireNonNull(event, "VideoProcessingEvent must not be null");
    UUID assetId = Objects.requireNonNull(event.assetId(), "assetId must not be null");
    File sourceFile = Objects.requireNonNull(event.file(), "source file must not be null");
    VideoQuality quality =
        Objects.requireNonNull(event.targetQuality(), "targetQuality must not be null");

    VideoAsset videoAsset =
        videoAssetRepository
            .findById(assetId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoAsset", assetId.toString()));

    Path inputTempFile = null;
    Path outputTempFile = null;

    // transcode the video using ffmpeg


    try {
      inputTempFile = Files.createTempFile("video-src-" + assetId + "-", ".tmp");
      outputTempFile =
          Files.createTempFile("video-out-" + assetId + "-" + quality.getLabel() + "-", ".mp4");

      try (InputStream sourceStream =
               fileService.download(sourceFile.getBucket(), sourceFile.getObjectName())) {
        Files.copy(sourceStream, inputTempFile, StandardCopyOption.REPLACE_EXISTING);
      }
      saveProgressToRedis(redisKey, new VideoProgressDto(0.0, 0.0, 0.0, "PROCESSING"));
      final double[] lastReportedProgress = {0.0};

      java.util.function.Consumer<VideoProgressDto> progressCallback = (progress) -> {
        saveProgressToRedis(redisKey, progress);
      };

      transcodeWithFfmpeg(inputTempFile, outputTempFile, quality, duration, progressCallback);

      // ... upload file to MinIO and update database ...
      saveVideoVariation(videoAsset);


      saveProgressToRedis(redisKey, new VideoProgressDto(100.0, 0.0, 0.0, "COMPLETED"));
    } catch (Exception e) {
      log.error(
          "Failed to process video variation: assetId={}, quality={}",
          assetId,
          quality.getLabel(),
          e);
      saveProgressToRedis(redisKey, new VideoProgressDto(0.0, 0.0, 0.0, "FAILED"));
      Video failedVideo = Video.builder().status(VideoStatus.NOT_AVAILABLE).file(null).build();
      updateVariationOnAsset(videoAsset, quality, failedVideo);
      videoAssetRepository.save(videoAsset);
      throw new ServiceException("Failed to transcode video variation " + quality.getLabel(), e);
    } finally {
      deleteTempFileQuietly(inputTempFile);
      deleteTempFileQuietly(outputTempFile);
    }
  }
  }
}
