package vn.io.oldmoon.shopizer.product.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;
import vn.io.oldmoon.shopizer.product.infra.repository.FileRepository;
import vn.io.oldmoon.shopizer.product.infra.repository.ImageAssetRepository;

@ExtendWith(MockitoExtension.class)
class ImageAssetServiceTest {

  @Mock private FileService fileService;
  @Mock private FileRepository fileRepository;
  @Mock private ImageAssetRepository imageAssetRepository;
  @Mock private TransactionTemplate transactionTemplate;
  @Mock private TransactionStatus transactionStatus;

  @InjectMocks private ImageAssetService imageAssetService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(imageAssetService, "mediaBucket", "media-public");
    lenient()
        .when(transactionTemplate.execute(any()))
        .thenAnswer(
            invocation -> {
              TransactionCallback<?> callback = invocation.getArgument(0);
              return callback.doInTransaction(transactionStatus);
            });
  }

  private byte[] createValidImageBytes(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.BLUE);
    g2d.fillRect(0, 0, width, height);
    g2d.dispose();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", baos);
    return baos.toByteArray();
  }

  @Nested
  @DisplayName("uploadImage(MultipartFile file)")
  class UploadImageTests {

    @Test
    @DisplayName("should throw NullPointerException when file is null")
    void uploadImage_WhenFileIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> imageAssetService.uploadImage(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("File must not be null");
    }

    @Test
    @DisplayName("should throw InvalidInputException when file is empty")
    void uploadImage_WhenFileIsEmpty_ShouldThrowInvalidInputException() {
      MockMultipartFile emptyFile =
          new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

      assertThatThrownBy(() -> imageAssetService.uploadImage(emptyFile))
          .isInstanceOf(InvalidInputException.class);
      verify(fileService, never()).save(any(), any());
    }

    @Test
    @DisplayName("should throw InvalidInputException when file content is not a valid image")
    void uploadImage_WhenContentIsInvalid_ShouldThrowInvalidInputException() {
      MockMultipartFile textFile =
          new MockMultipartFile("image", "fake.jpg", "image/jpeg", "not an image".getBytes());

      assertThatThrownBy(() -> imageAssetService.uploadImage(textFile))
          .isInstanceOf(InvalidInputException.class);
      verify(fileService, never()).save(any(), any());
    }

    @Test
    @DisplayName("should resize, upload 4 files, persist ImageAsset, and activate files")
    void uploadImage_WhenValid_ShouldUploadAndPersistAsset() throws Exception {
      byte[] imageBytes = createValidImageBytes(1000, 800);
      MockMultipartFile multipartFile =
          new MockMultipartFile("image", "gpu.jpg", "image/jpeg", imageBytes);

      when(fileService.save(any(InputStream.class), any(File.class)))
          .thenAnswer(
              invocation -> {
                File f = invocation.getArgument(1);
                f.setId(UUID.randomUUID());
                return f;
              });

      when(imageAssetRepository.save(any(ImageAsset.class)))
          .thenAnswer(
              invocation -> {
                ImageAsset asset = invocation.getArgument(0);
                asset.setId(UUID.randomUUID());
                return asset;
              });

      ImageAsset result = imageAssetService.uploadImage(multipartFile);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNotNull();
      assertThat(result.getOriginal()).isNotNull();
      assertThat(result.getThumbnail()).isNotNull();
      assertThat(result.getMedium()).isNotNull();
      assertThat(result.getLarge()).isNotNull();

      assertThat(result.getOriginal().getStatus()).isEqualTo(FileStatus.ACTIVE);
      assertThat(result.getThumbnail().getStatus()).isEqualTo(FileStatus.ACTIVE);
      assertThat(result.getMedium().getStatus()).isEqualTo(FileStatus.ACTIVE);
      assertThat(result.getLarge().getStatus()).isEqualTo(FileStatus.ACTIVE);

      verify(fileService, times(4)).save(any(InputStream.class), any(File.class));
      verify(imageAssetRepository).save(any(ImageAsset.class));
      verify(fileRepository)
          .updateStatusByIds(
              eq(
                  List.of(
                      result.getOriginal().getId(),
                      result.getThumbnail().getId(),
                      result.getMedium().getId(),
                      result.getLarge().getId())),
              eq(FileStatus.ACTIVE));
    }
  }

  @Nested
  @DisplayName("get(UUID assetId)")
  class GetTests {

    @Test
    @DisplayName("should throw NullPointerException when assetId is null")
    void get_WhenAssetIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> imageAssetService.get(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Asset ID must not be null");
    }

    @Test
    @DisplayName("should return ImageAsset when found")
    void get_WhenFound_ShouldReturnImageAsset() {
      UUID id = UUID.randomUUID();
      ImageAsset asset = ImageAsset.builder().build();
      asset.setId(id);
      when(imageAssetRepository.findById(id)).thenReturn(Optional.of(asset));

      ImageAsset result = imageAssetService.get(id);

      assertThat(result).isSameAs(asset);
      verify(imageAssetRepository).findById(id);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when ImageAsset does not exist")
    void get_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(imageAssetRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> imageAssetService.get(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("ImageAsset")
          .hasMessageContaining(id.toString());
    }
  }

  @Nested
  @DisplayName("delete(UUID id)")
  class DeleteTests {

    @Test
    @DisplayName("should throw NullPointerException when id is null")
    void delete_WhenIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> imageAssetService.delete(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("ID must not be null");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when ImageAsset does not exist")
    void delete_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(imageAssetRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> imageAssetService.delete(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("ImageAsset")
          .hasMessageContaining(id.toString());

      verify(imageAssetRepository, never()).delete(any());
      verify(fileService, never()).delete(any());
    }

    @Test
    @DisplayName("should delete ImageAsset and all four associated files")
    void delete_WhenFound_ShouldDeleteAssetAndFiles() {
      UUID assetId = UUID.randomUUID();
      File original = File.builder().bucket("media-public").objectName("orig.jpg").build();
      original.setId(UUID.randomUUID());
      File thumbnail = File.builder().bucket("media-public").objectName("thumb.jpg").build();
      thumbnail.setId(UUID.randomUUID());
      File medium = File.builder().bucket("media-public").objectName("med.jpg").build();
      medium.setId(UUID.randomUUID());
      File large = File.builder().bucket("media-public").objectName("large.jpg").build();
      large.setId(UUID.randomUUID());

      ImageAsset imageAsset =
          ImageAsset.builder()
              .original(original)
              .thumbnail(thumbnail)
              .medium(medium)
              .large(large)
              .build();
      imageAsset.setId(assetId);

      when(imageAssetRepository.findById(assetId)).thenReturn(Optional.of(imageAsset));

      imageAssetService.delete(assetId);

      verify(imageAssetRepository).delete(imageAsset);
      verify(fileService).delete(original.getId());
      verify(fileService).delete(thumbnail.getId());
      verify(fileService).delete(medium.getId());
      verify(fileService).delete(large.getId());
    }
  }
}
