package vn.io.oldmoon.shopizer.common.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;

class ImageUtilTest {

  private byte[] createValidImageBytes(int width, int height, String format) {
    try {
      BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(img, format, baos);
      return baos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  @DisplayName("validateBasic(MultipartFile file)")
  class ValidateBasicTest {

    @Test
    @DisplayName("should throw InvalidInputException when file is null")
    void validateBasic_WhenNull_ShouldThrow() {
      assertThatThrownBy(() -> ImageUtil.validateBasic(null))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("The file is empty.");
    }

    @Test
    @DisplayName("should throw InvalidInputException when file is empty")
    void validateBasic_WhenEmpty_ShouldThrow() {
      MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);
      assertThatThrownBy(() -> ImageUtil.validateBasic(file))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("The file is empty.");
    }

    @Test
    @DisplayName("should throw InvalidInputException when file size exceeds 5MB")
    void validateBasic_WhenTooLarge_ShouldThrow() {
      byte[] large = new byte[5 * 1024 * 1024 + 1];
      MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", large);
      assertThatThrownBy(() -> ImageUtil.validateBasic(file))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Image exceeds maximum size of 5MB");
    }

    @Test
    @DisplayName("should pass when file size is valid")
    void validateBasic_WhenValid_ShouldNotThrow() {
      MockMultipartFile file = new MockMultipartFile("file", "ok.png", "image/png", new byte[100]);
      assertThatCode(() -> ImageUtil.validateBasic(file)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("validateContent(MultipartFile file)")
  class ValidateContentTest {

    @Test
    @DisplayName("should throw InvalidInputException when content is text/plain")
    void validateContent_WhenNotImage_ShouldThrow() {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", "Hello world".getBytes());
      assertThatThrownBy(() -> ImageUtil.validateContent(file))
          .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("should pass when content is valid PNG")
    void validateContent_WhenValidPng_ShouldPass() {
      byte[] pngBytes = createValidImageBytes(100, 100, "png");
      MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", pngBytes);
      assertThatCode(() -> ImageUtil.validateContent(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should pass when content is valid JPEG")
    void validateContent_WhenValidJpeg_ShouldPass() {
      byte[] jpgBytes = createValidImageBytes(100, 100, "jpg");
      MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpgBytes);
      assertThatCode(() -> ImageUtil.validateContent(file)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("resize(MultipartFile file, int width, int height)")
  class ResizeTest {

    @Test
    @DisplayName("should resize valid image to requested dimensions")
    void resize_WhenValidImage_ShouldReturnResizedBytes() throws Exception {
      byte[] pngBytes = createValidImageBytes(400, 400, "png");
      MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", pngBytes);

      byte[] resized = ImageUtil.resize(file, 100, 100);

      assertThat(resized).isNotEmpty();
      BufferedImage resultImg = ImageIO.read(new java.io.ByteArrayInputStream(resized));
      assertThat(resultImg).isNotNull();
      assertThat(resultImg.getWidth()).isEqualTo(100);
      assertThat(resultImg.getHeight()).isEqualTo(100);
    }

    @Test
    @DisplayName("resizeImageToStream should return non-null readable stream")
    void resizeImageToStream_ShouldReturnReadableStream() throws Exception {
      byte[] pngBytes = createValidImageBytes(200, 200, "png");
      MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", pngBytes);

      try (InputStream stream = ImageUtil.resizeImageToStream(file, 50, 50)) {
        assertThat(stream).isNotNull();
        byte[] readBytes = stream.readAllBytes();
        assertThat(readBytes).isNotEmpty();
      }
    }
  }
}
