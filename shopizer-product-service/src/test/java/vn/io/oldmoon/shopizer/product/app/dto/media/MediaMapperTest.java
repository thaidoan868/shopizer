package vn.io.oldmoon.shopizer.product.app.dto.media;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Visibility;

class MediaMapperTest {

  private final MediaMapper mediaMapper = Mappers.getMapper(MediaMapper.class);

  @Test
  @DisplayName("toDto should return null when file is null")
  void toDto_WhenFileIsNull_ShouldReturnNull() {
    assertThat(mediaMapper.toDto(null)).isNull();
  }

  @Test
  @DisplayName("toDto should map valid File entity to FileResponse")
  void toDto_WhenFileIsValid_ShouldMapToFileResponse() {
    UUID id = UUID.randomUUID();
    File file =
        File.builder()
            .bucket("media-public")
            .objectName("image-original-550e8400-rtx4090.jpg")
            .sizeBytes(2458120L)
            .contentType("image/jpeg")
            .visibility(Visibility.PUBLIC)
            .status(FileStatus.ACTIVE)
            .build();
    file.setId(id);

    FileResponse response = mediaMapper.toDto(file);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getBucket()).isEqualTo("media-public");
    assertThat(response.getObjectName()).isEqualTo("image-original-550e8400-rtx4090.jpg");
    assertThat(response.getSizeBytes()).isEqualTo(2458120L);
    assertThat(response.getContentType()).isEqualTo("image/jpeg");
    assertThat(response.getVisibility()).isEqualTo(Visibility.PUBLIC);
    assertThat(response.getStatus()).isEqualTo(FileStatus.ACTIVE);
    assertThat(response.getUrl()).isNull(); // URL is populated by Populator
  }
}
