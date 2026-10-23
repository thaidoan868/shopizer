package vn.io.oldmoon.shopizer.product.app.dto.media;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.Visibility;

class FileMapperTest {

  private final FileMapper fileMapper = Mappers.getMapper(FileMapper.class);

  @Test
  @DisplayName("toDto should return null when file is null")
  void toDto_WhenFileIsNull_ShouldReturnNull() {
    assertThat(fileMapper.toDto(null)).isNull();
  }

  @Test
  @DisplayName("toDto should map File entity to FileResponse ignoring url")
  void toDto_WhenFileIsValid_ShouldMapToDto() {
    UUID id = UUID.randomUUID();
    File file =
        File.builder()
            .bucket("media-public")
            .objectName("image-1.jpg")
            .sizeBytes(1024L)
            .contentType("image/jpeg")
            .visibility(Visibility.PUBLIC)
            .status(FileStatus.ACTIVE)
            .build();
    file.setId(id);

    FileResponse response = fileMapper.toDto(file);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getBucket()).isEqualTo("media-public");
    assertThat(response.getObjectName()).isEqualTo("image-1.jpg");
    assertThat(response.getSizeBytes()).isEqualTo(1024L);
    assertThat(response.getContentType()).isEqualTo("image/jpeg");
    assertThat(response.getVisibility()).isEqualTo(Visibility.PUBLIC);
    assertThat(response.getStatus()).isEqualTo(FileStatus.ACTIVE);
    assertThat(response.getUrl()).isNull();
  }
}
