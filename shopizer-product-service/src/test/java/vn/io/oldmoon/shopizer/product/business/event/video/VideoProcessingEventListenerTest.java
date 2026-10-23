package vn.io.oldmoon.shopizer.product.business.event.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.product.business.service.FileService;
import vn.io.oldmoon.shopizer.product.business.service.VideoAssetService;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.Video;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoQuality;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoStatus;

@ExtendWith(MockitoExtension.class)
class VideoProcessingEventListenerTest {

  @Mock private VideoAssetService videoAssetService;
  @Mock private FileService fileService;

  @InjectMocks private VideoProcessingEventListener listener;

  @Test
  @DisplayName("handle should throw NullPointerException when event is null")
  void handle_WhenEventIsNull_ShouldThrowNpe() {
    assertThatThrownBy(() -> listener.handle(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("VideoProcessingEvent must not be null");
  }

  @Test
  @DisplayName("handle should download source video, transcode, and save variation when successful")
  void handle_WhenValidEvent_ShouldTranscodeAndSaveVariation() throws Exception {
    UUID assetId = UUID.randomUUID();
    File sourceFile = File.builder().bucket("media-public").objectName("orig.mp4").build();
    sourceFile.setId(UUID.randomUUID());
    VideoAsset asset = VideoAsset.builder().build();
    asset.setId(assetId);

    when(videoAssetService.get(assetId)).thenReturn(asset);
    when(fileService.download("media-public", "orig.mp4"))
        .thenReturn(new ByteArrayInputStream("video-bytes".getBytes()));

    VideoProcessingEvent event =
        new VideoProcessingEvent(assetId, sourceFile, VideoQuality.P720);

    listener.handle(event);

    verify(videoAssetService).get(assetId);
    verify(videoAssetService)
        .updateVariationOnAsset(eq(asset), eq(VideoQuality.P720), any(Video.class));
    verify(fileService).download("media-public", "orig.mp4");
    verify(videoAssetService)
        .transcodeWithFfmpeg(any(Path.class), any(Path.class), eq(VideoQuality.P720));
    verify(videoAssetService)
        .saveVideoVariation(eq(assetId), eq(VideoQuality.P720), any(Path.class));
  }

  @Test
  @DisplayName("handle should mark variation NOT_AVAILABLE and rethrow ServiceException when transcoding fails")
  void handle_WhenTranscodingFails_ShouldMarkNotAvailableAndThrowServiceException()
      throws Exception {
    UUID assetId = UUID.randomUUID();
    File sourceFile = File.builder().bucket("media-public").objectName("orig.mp4").build();
    sourceFile.setId(UUID.randomUUID());
    VideoAsset asset = VideoAsset.builder().build();
    asset.setId(assetId);

    when(videoAssetService.get(assetId)).thenReturn(asset);
    when(fileService.download("media-public", "orig.mp4"))
        .thenReturn(new ByteArrayInputStream("video-bytes".getBytes()));
    doThrow(new ServiceException("FFmpeg failed"))
        .when(videoAssetService)
        .transcodeWithFfmpeg(any(Path.class), any(Path.class), eq(VideoQuality.P360));

    VideoProcessingEvent event =
        new VideoProcessingEvent(assetId, sourceFile, VideoQuality.P360);

    assertThatThrownBy(() -> listener.handle(event))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("Failed to transcode video variation 360p");

    ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
    verify(videoAssetService, times(2))
        .updateVariationOnAsset(eq(asset), eq(VideoQuality.P360), videoCaptor.capture());
    assertThat(videoCaptor.getValue().getStatus()).isEqualTo(VideoStatus.NOT_AVAILABLE);
  }
}
