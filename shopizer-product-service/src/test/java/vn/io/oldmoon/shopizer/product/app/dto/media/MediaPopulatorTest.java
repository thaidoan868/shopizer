package vn.io.oldmoon.shopizer.product.app.dto.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.Video;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoStatus;

@ExtendWith(MockitoExtension.class)
class MediaPopulatorTest {

  @Mock private FileMapper fileMapper;

  @InjectMocks private FilePopulator filePopulator;

  private ImageAssetPopulator imageAssetPopulator;
  private VideoAssetPopulator videoAssetPopulator;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(filePopulator, "mediaEndpoint", "http://localhost:9000");
    imageAssetPopulator = new ImageAssetPopulator(filePopulator);
    videoAssetPopulator = new VideoAssetPopulator(filePopulator);
  }

  @Test
  @DisplayName("FilePopulator.toDto should throw NullPointerException when file is null")
  void filePopulator_WhenNull_ShouldThrowNpe() {
    assertThatThrownBy(() -> filePopulator.toDto(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("File must not be null");
  }

  @Test
  @DisplayName("FilePopulator.toDto should populate URL on mapped FileResponse")
  void filePopulator_WhenValid_ShouldPopulateUrl() {
    UUID fileId = UUID.randomUUID();
    File file = File.builder().bucket("media-public").objectName("my image.jpg").build();
    file.setId(fileId);

    FileResponse mapped = FileResponse.builder().id(fileId).bucket("media-public").objectName("my image.jpg").build();
    when(fileMapper.toDto(file)).thenReturn(mapped);

    FileResponse result = filePopulator.toDto(file);

    assertThat(result.getUrl()).isEqualTo("http://localhost:9000/media-public/my%20image.jpg");
    verify(fileMapper).toDto(file);
  }

  @Test
  @DisplayName("ImageAssetPopulator.toDto should map all 4 file variations")
  void imageAssetPopulator_WhenValid_ShouldMapAllVariations() {
    UUID assetId = UUID.randomUUID();
    File orig = File.builder().bucket("media-public").objectName("orig.jpg").build();
    File thumb = File.builder().bucket("media-public").objectName("thumb.jpg").build();
    File med = File.builder().bucket("media-public").objectName("med.jpg").build();
    File large = File.builder().bucket("media-public").objectName("large.jpg").build();

    when(fileMapper.toDto(orig)).thenReturn(FileResponse.builder().objectName("orig.jpg").build());
    when(fileMapper.toDto(thumb)).thenReturn(FileResponse.builder().objectName("thumb.jpg").build());
    when(fileMapper.toDto(med)).thenReturn(FileResponse.builder().objectName("med.jpg").build());
    when(fileMapper.toDto(large)).thenReturn(FileResponse.builder().objectName("large.jpg").build());

    ImageAsset asset =
        ImageAsset.builder().original(orig).thumbnail(thumb).medium(med).large(large).build();
    asset.setId(assetId);

    CreateImageAssetResponse response = imageAssetPopulator.toDto(asset);

    assertThat(response.getId()).isEqualTo(assetId);
    assertThat(response.getOriginal().getUrl())
        .isEqualTo("http://localhost:9000/media-public/orig.jpg");
    assertThat(response.getThumbnail().getUrl())
        .isEqualTo("http://localhost:9000/media-public/thumb.jpg");
    assertThat(response.getMedium().getUrl())
        .isEqualTo("http://localhost:9000/media-public/med.jpg");
    assertThat(response.getLarge().getUrl())
        .isEqualTo("http://localhost:9000/media-public/large.jpg");
  }

  @Test
  @DisplayName("VideoAssetPopulator.toDto should map VideoAsset variations")
  void videoAssetPopulator_WhenValid_ShouldMapVariations() {
    UUID assetId = UUID.randomUUID();
    File origFile = File.builder().bucket("media-public").objectName("orig.mp4").build();
    when(fileMapper.toDto(origFile))
        .thenReturn(FileResponse.builder().objectName("orig.mp4").build());

    VideoAsset asset =
        VideoAsset.builder()
            .original(
                Video.builder()
                    .status(VideoStatus.AVAILABLE)
                    .duration(60.0)
                    .file(origFile)
                    .build())
            .p360(Video.builder().status(VideoStatus.NOT_AVAILABLE).file(null).build())
            .p720(null)
            .p1080(null)
            .build();
    asset.setId(assetId);

    CreateVideoAssetResponse response = videoAssetPopulator.toDto(asset);

    assertThat(response.getId()).isEqualTo(assetId);
    assertThat(response.getOriginal().getStatus()).isEqualTo(VideoStatus.AVAILABLE);
    assertThat(response.getOriginal().getDuration()).isEqualTo(60.0);
    assertThat(response.getOriginal().getFile().getUrl())
        .isEqualTo("http://localhost:9000/media-public/orig.mp4");
    assertThat(response.getP360().getStatus()).isEqualTo(VideoStatus.NOT_AVAILABLE);
    assertThat(response.getP360().getFile()).isNull();
    assertThat(response.getP720()).isNull();
    assertThat(response.getP1080()).isNull();
  }
}
