package vn.io.oldmoon.shopizer.product.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.event.EventPublisher;
import vn.io.oldmoon.shopizer.common.core.event.RabbitMqEvent;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.common.core.exception.ServiceException;
import vn.io.oldmoon.shopizer.common.core.utility.VideoUtil;
import vn.io.oldmoon.shopizer.product.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.product.business.event.video.VideoProcessingEvent;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Video;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoQuality;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoStatus;
import vn.io.oldmoon.shopizer.product.infra.repository.FileRepository;
import vn.io.oldmoon.shopizer.product.infra.repository.VideoAssetRepository;

@ExtendWith(MockitoExtension.class)
class VideoAssetServiceTest {

  @Mock private FileService fileService;
  @Mock private FileRepository fileRepository;
  @Mock private VideoAssetRepository videoAssetRepository;
  @Mock private EventPublisher eventPublisher;
  @Mock private TransactionTemplate transactionTemplate;
  @Mock private TransactionStatus transactionStatus;

  @InjectMocks private VideoAssetService videoAssetService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(videoAssetService, "mediaBucket", "media-public");
    lenient()
        .when(transactionTemplate.execute(any()))
        .thenAnswer(
            invocation -> {
              TransactionCallback<?> callback = invocation.getArgument(0);
              return callback.doInTransaction(transactionStatus);
            });
    lenient()
        .doAnswer(
            invocation -> {
              Consumer<TransactionStatus> consumer = invocation.getArgument(0);
              consumer.accept(transactionStatus);
              return null;
            })
        .when(transactionTemplate)
        .executeWithoutResult(any());
  }

  @Nested
  @DisplayName("uploadVideo(MultipartFile file)")
  class UploadVideoTests {

    @Test
    @DisplayName("should throw NullPointerException when file is null")
    void uploadVideo_WhenFileIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> videoAssetService.uploadVideo(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("File must not be null");
    }

    @Test
    @DisplayName("should upload original video, persist VideoAsset, activate file, and publish events")
    void uploadVideo_WhenValid_ShouldUploadPersistAndPublishEvents() {
      MockMultipartFile videoFile =
          new MockMultipartFile("video", "demo.mp4", "video/mp4", "fake-video-bytes".getBytes());

      try (MockedStatic<VideoUtil> videoUtilMock = mockStatic(VideoUtil.class)) {
        videoUtilMock.when(() -> VideoUtil.validateContent(any(MultipartFile.class))).thenAnswer(i -> null);
        videoUtilMock.when(() -> VideoUtil.extractDuration(any(MultipartFile.class))).thenReturn(45.5);

        when(fileService.save(any(InputStream.class), any(File.class)))
            .thenAnswer(
                invocation -> {
                  File f = invocation.getArgument(1);
                  f.setId(UUID.randomUUID());
                  return f;
                });

        when(videoAssetRepository.save(any(VideoAsset.class)))
            .thenAnswer(
                invocation -> {
                  VideoAsset asset = invocation.getArgument(0);
                  asset.setId(UUID.randomUUID());
                  return asset;
                });

        VideoAsset result = videoAssetService.uploadVideo(videoFile);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOriginal()).isNotNull();
        assertThat(result.getOriginal().getStatus()).isEqualTo(VideoStatus.AVAILABLE);
        assertThat(result.getOriginal().getDuration()).isEqualTo(45.5);
        assertThat(result.getOriginal().getFile().getStatus()).isEqualTo(FileStatus.ACTIVE);
        assertThat(result.getP360().getStatus()).isEqualTo(VideoStatus.NOT_AVAILABLE);
        assertThat(result.getP720().getStatus()).isEqualTo(VideoStatus.NOT_AVAILABLE);
        assertThat(result.getP1080().getStatus()).isEqualTo(VideoStatus.NOT_AVAILABLE);

        verify(fileService).save(any(InputStream.class), any(File.class));
        verify(videoAssetRepository).save(any(VideoAsset.class));
        verify(fileRepository).save(result.getOriginal().getFile());
        verify(eventPublisher, times(VideoQuality.values().length))
            .publish(any(RabbitMqEvent.class));
      }
    }
  }

  @Nested
  @DisplayName("get(UUID assetId)")
  class GetTests {

    @Test
    @DisplayName("should throw NullPointerException when assetId is null")
    void get_WhenAssetIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> videoAssetService.get(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Asset ID must not be null");
    }

    @Test
    @DisplayName("should return VideoAsset when found")
    void get_WhenFound_ShouldReturnVideoAsset() {
      UUID id = UUID.randomUUID();
      VideoAsset asset = VideoAsset.builder().build();
      asset.setId(id);
      when(videoAssetRepository.findById(id)).thenReturn(Optional.of(asset));

      VideoAsset result = videoAssetService.get(id);

      assertThat(result).isSameAs(asset);
      verify(videoAssetRepository).findById(id);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when VideoAsset does not exist")
    void get_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(videoAssetRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> videoAssetService.get(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("VideoAsset")
          .hasMessageContaining(id.toString());
    }
  }

  @Nested
  @DisplayName("saveVideoVariation(UUID assetId, VideoQuality quality, Path outputTempFile)")
  class SaveVideoVariationTests {

    @Test
    @DisplayName("should upload variation file, update VideoAsset variation, and activate file")
    void saveVideoVariation_WhenValid_ShouldUploadAndUpdateAsset(@TempDir Path tempDir)
        throws Exception {
      UUID assetId = UUID.randomUUID();
      Path outputTempFile = tempDir.resolve("out-720p.mp4");
      Files.write(outputTempFile, "transcoded-bytes".getBytes());

      VideoAsset existingAsset = VideoAsset.builder().build();
      existingAsset.setId(assetId);
      when(videoAssetRepository.findById(assetId)).thenReturn(Optional.of(existingAsset));

      when(fileService.save(any(InputStream.class), any(File.class)))
          .thenAnswer(
              invocation -> {
                File f = invocation.getArgument(1);
                f.setId(UUID.randomUUID());
                return f;
              });

      try (MockedStatic<VideoUtil> videoUtilMock = mockStatic(VideoUtil.class)) {
        videoUtilMock.when(() -> VideoUtil.extractDuration(outputTempFile)).thenReturn(30.0);

        videoAssetService.saveVideoVariation(assetId, VideoQuality.P720, outputTempFile);

        assertThat(existingAsset.getP720()).isNotNull();
        assertThat(existingAsset.getP720().getStatus()).isEqualTo(VideoStatus.AVAILABLE);
        assertThat(existingAsset.getP720().getDuration()).isEqualTo(30.0);
        assertThat(existingAsset.getP720().getFile()).isNotNull();
        assertThat(existingAsset.getP720().getFile().getStatus()).isEqualTo(FileStatus.ACTIVE);

        verify(videoAssetRepository).save(existingAsset);
        verify(fileRepository).save(existingAsset.getP720().getFile());
      }
    }
  }

  @Nested
  @DisplayName("delete(UUID id)")
  class DeleteTests {

    @Test
    @DisplayName("should throw NullPointerException when id is null")
    void delete_WhenIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> videoAssetService.delete(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("ID must not be null");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when VideoAsset does not exist")
    void delete_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(videoAssetRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> videoAssetService.delete(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("VideoAsset")
          .hasMessageContaining(id.toString());

      verify(videoAssetRepository, never()).delete(any());
      verify(fileService, never()).delete(any());
    }

    @Test
    @DisplayName("should delete all available variation files and hard delete VideoAsset")
    void delete_WhenFound_ShouldDeleteFilesAndAsset() {
      UUID assetId = UUID.randomUUID();
      File origFile = File.builder().bucket("media-public").objectName("orig.mp4").build();
      origFile.setId(UUID.randomUUID());
      File p360File = File.builder().bucket("media-public").objectName("360.mp4").build();
      p360File.setId(UUID.randomUUID());

      VideoAsset asset =
          VideoAsset.builder()
              .original(Video.builder().status(VideoStatus.AVAILABLE).file(origFile).build())
              .p360(Video.builder().status(VideoStatus.AVAILABLE).file(p360File).build())
              .p720(Video.builder().status(VideoStatus.NOT_AVAILABLE).file(null).build())
              .p1080(null)
              .build();
      asset.setId(assetId);

      when(videoAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));

      videoAssetService.delete(assetId);

      verify(fileService).delete(origFile.getId());
      verify(fileService).delete(p360File.getId());
      verify(fileService, times(2)).delete(any());
      verify(videoAssetRepository).delete(asset);
    }
  }

  @Nested
  @DisplayName("updateVariationOnAsset(VideoAsset videoAsset, VideoQuality quality, Video variation)")
  class UpdateVariationTests {

    @Test
    @DisplayName("should update P360, P720, and P1080 variations and save VideoAsset")
    void updateVariationOnAsset_ShouldSetCorrectQualityAndSave() {
      VideoAsset asset = VideoAsset.builder().build();
      asset.setId(UUID.randomUUID());
      Video v360 = Video.builder().status(VideoStatus.PROCESSING).build();
      Video v720 = Video.builder().status(VideoStatus.AVAILABLE).build();
      Video v1080 = Video.builder().status(VideoStatus.NOT_AVAILABLE).build();

      videoAssetService.updateVariationOnAsset(asset, VideoQuality.P360, v360);
      videoAssetService.updateVariationOnAsset(asset, VideoQuality.P720, v720);
      videoAssetService.updateVariationOnAsset(asset, VideoQuality.P1080, v1080);

      assertThat(asset.getP360()).isSameAs(v360);
      assertThat(asset.getP720()).isSameAs(v720);
      assertThat(asset.getP1080()).isSameAs(v1080);
      verify(videoAssetRepository, times(3)).save(asset);
    }
  }

  @Nested
  @DisplayName("transcodeWithFfmpeg(Path inputPath, Path outputPath, VideoQuality quality)")
  class TranscodeWithFfmpegTests {

    private Path generateSampleVideo(Path tempDir) throws Exception {
      Path sampleInput = tempDir.resolve("sample-source-1080p.mp4");
      ProcessBuilder pb =
          new ProcessBuilder(
              "ffmpeg",
              "-y",
              "-f",
              "lavfi",
              "-i",
              "testsrc=duration=1:size=1920x1080:rate=24",
              "-f",
              "lavfi",
              "-i",
              "sine=frequency=440:duration=1",
              "-c:v",
              "libx264",
              "-pix_fmt",
              "yuv420p",
              "-preset",
              "ultrafast",
              "-c:a",
              "aac",
              sampleInput.toAbsolutePath().toString());
      pb.redirectErrorStream(true);
      Process process = pb.start();
      process.getInputStream().readAllBytes();
      int exitCode = process.waitFor();
      assertThat(exitCode).isZero();
      return sampleInput;
    }

    private String probeVideoStreamProperty(Path videoFile, String entry) throws Exception {
      ProcessBuilder pb =
          new ProcessBuilder(
              "ffprobe",
              "-v",
              "error",
              "-select_streams",
              "v:0",
              "-show_entries",
              "stream=" + entry,
              "-of",
              "default=noprint_wrappers=1:nokey=1",
              videoFile.toAbsolutePath().toString());
      pb.redirectErrorStream(true);
      Process process = pb.start();
      String output;
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        output = reader.lines().collect(Collectors.joining()).trim();
      }
      int exitCode = process.waitFor();
      assertThat(exitCode).isZero();
      return output;
    }

    @Test
    @DisplayName("should throw NullPointerException when arguments are null")
    void transcodeWithFfmpeg_WhenNullArguments_ShouldThrowNpe(@TempDir Path tempDir) {
      Path dummyPath = tempDir.resolve("dummy.mp4");

      assertThatThrownBy(
              () -> videoAssetService.transcodeWithFfmpeg(null, dummyPath, VideoQuality.P720))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Input path must not be null");

      assertThatThrownBy(
              () -> videoAssetService.transcodeWithFfmpeg(dummyPath, null, VideoQuality.P720))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Output path must not be null");

      assertThatThrownBy(() -> videoAssetService.transcodeWithFfmpeg(dummyPath, dummyPath, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Video quality must not be null");
    }

    @ParameterizedTest
    @EnumSource(VideoQuality.class)
    @DisplayName("should transcode real video to target resolution, h264 codec, and yuv420p pixel format")
    void transcodeWithFfmpeg_WithRealVideo_ShouldTranscodeToTargetQuality(
        VideoQuality quality, @TempDir Path tempDir) throws Exception {
      Path inputPath = generateSampleVideo(tempDir);
      Path outputPath = tempDir.resolve("output-" + quality.getLabel() + ".mp4");

      videoAssetService.transcodeWithFfmpeg(inputPath, outputPath, quality);

      assertThat(Files.exists(outputPath)).isTrue();
      assertThat(Files.size(outputPath)).isGreaterThan(0L);

      int actualHeight = Integer.parseInt(probeVideoStreamProperty(outputPath, "height"));
      String codecName = probeVideoStreamProperty(outputPath, "codec_name");
      String pixFmt = probeVideoStreamProperty(outputPath, "pix_fmt");
      double duration = VideoUtil.extractDuration(outputPath);

      assertThat(actualHeight).isEqualTo(quality.getHeight());
      assertThat(codecName).isEqualTo("h264");
      assertThat(pixFmt).isEqualTo("yuv420p");
      assertThat(duration).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("should throw ServiceException when input file is corrupted or not a valid video")
    void transcodeWithFfmpeg_WhenCorruptedInput_ShouldThrowServiceException(@TempDir Path tempDir)
        throws Exception {
      Path corruptedInput = tempDir.resolve("corrupted.mp4");
      Files.writeString(corruptedInput, "not-a-valid-video-stream");
      Path outputPath = tempDir.resolve("output.mp4");

      assertThatThrownBy(
              () ->
                  videoAssetService.transcodeWithFfmpeg(
                      corruptedInput, outputPath, VideoQuality.P360))
          .isInstanceOf(ServiceException.class)
          .hasMessageStartingWith("FFmpeg failed:");
    }
  }
}
