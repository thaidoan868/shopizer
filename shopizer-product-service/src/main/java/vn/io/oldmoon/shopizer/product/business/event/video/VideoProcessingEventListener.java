package vn.io.oldmoon.shopizer.product.business.event.video;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.core.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.common.core.utility.FileUtil;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.product.business.service.FileService;
import vn.io.oldmoon.shopizer.product.business.service.VideoAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingEventListener
    implements ApplicationEventListener<VideoProcessingEvent> {

  private final VideoAssetService videoAssetService;
  private final FileService fileService;

  @Override
  @RabbitListener(
      queues = RabbitMqConfig.videoProcessingQueue,
      containerFactory = "videoRabbitListenerContainerFactory")
  public void handle(VideoProcessingEvent event) {
    Objects.requireNonNull(event, "VideoProcessingEvent must not be null");
    UUID assetId = Objects.requireNonNull(event.assetId(), "assetId must not be null");
    File sourceFile = Objects.requireNonNull(event.file(), "source file must not be null");
    VideoQuality quality =
        Objects.requireNonNull(event.targetQuality(), "targetQuality must not be null");

    log.info("Received VideoProcessingEvent for assetId={}, quality={}", assetId, quality);

    Path inputTempFile = null;
    Path outputTempFile = null;
    Video video = Video.builder().status(VideoStatus.PROCESSING).build();
    VideoAsset videoAsset = videoAssetService.get(assetId);
    videoAssetService.updateVariationOnAsset(videoAsset, quality, video);

    // transcode the video to the target quality and save the result
    try {
      inputTempFile = Files.createTempFile("video-src-" + assetId + "-", ".tmp");
      outputTempFile =
          Files.createTempFile("video-out-" + assetId + "-" + quality.getLabel() + "-", ".mp4");

      try (InputStream sourceStream =
          fileService.download(sourceFile.getBucket(), sourceFile.getObjectName())) {
        Files.copy(sourceStream, inputTempFile, StandardCopyOption.REPLACE_EXISTING);
        log.debug(
            "Downloaded source video to temporary file: Path: {} (size: {} bytes)",
            inputTempFile,
            Files.size(inputTempFile));
      }

      videoAssetService.transcodeWithFfmpeg(inputTempFile, outputTempFile, quality);

      videoAssetService.saveVideoVariation(assetId, quality, outputTempFile);
    } catch (Exception e) {
      log.error(
          "Failed to process video variation: assetId={}, quality={}",
          assetId,
          quality.getLabel(),
          e);
      video.setStatus(VideoStatus.NOT_AVAILABLE);
      videoAssetService.updateVariationOnAsset(videoAsset, quality, video);
      throw new ServiceException("Failed to transcode video variation " + quality.getLabel(), e);
    } finally {
      FileUtil.deleteTempFileQuietly(inputTempFile);
      FileUtil.deleteTempFileQuietly(outputTempFile);
    }
  }
}
