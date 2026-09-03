package vn.io.oldmoon.shopizer.common.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;

class ImageUtilTest {

  private static byte[] createValidImageBytes(int width, int height, String format)
      throws IOException {
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, format, baos);
    return baos.toByteArray();
  }

  @Nested
  @DisplayName("validateBasic(MultipartFile file)")
  class ValidateBasicTest {

    @Test
    @DisplayName("should throw InvalidInputException when file is null")
    void validateBasic_WhenFileIsNull_ShouldThrowException() {
      assertThatThrownBy(() -> ImageUtil.validateBasic(null))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("The file is empty.");
    }

    @Test
    @DisplayName("should throw InvalidInputException when file is empty")
    void validateBasic_WhenFileIsEmpty_ShouldThrowException() {
      MockMultipartFile emptyFile =
          new MockMultipartFile("file", "test.png", "image/png", new byte[0]);
      assertThatThrownBy(() -> ImageUtil.validateBasic(emptyFile))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("The file is empty.");
    }

    @Test
    @DisplayName("should throw InvalidInputException when file exceeds 5MB")
    void validateBasic_WhenFileExceeds5Mb_ShouldThrowException() {
      byte[] largeContent = new byte[5 * 1024 * 1024 + 1];
      MockMultipartFile largeFile =
          new MockMultipartFile("file", "large.png", "image/png", largeContent);

      assertThatThrownBy(() -> ImageUtil.validateBasic(largeFile))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Image exceeds maximum size of 5MB");
    }

    @Test
    @DisplayName("should pass validation when file is valid and within size limit")
    void validateBasic_WhenFileIsValid_ShouldPass() {
      MockMultipartFile validFile =
          new MockMultipartFile("file", "valid.png", "image/png", new byte[100]);
      assertThatCode(() -> ImageUtil.validateBasic(validFile)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("validateContent(MultipartFile file)")
  class ValidateContentTest {

    @Test
    @DisplayName("should pass validation for valid PNG image")
    void validateContent_WhenValidPng_ShouldPass() throws Exception {
      byte[] bytes = createValidImageBytes(100, 100, "png");
      MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", bytes);

      assertThatCode(() -> ImageUtil.validateContent(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should pass validation for valid JPEG image")
    void validateContent_WhenValidJpeg_ShouldPass() throws Exception {
      byte[] bytes = createValidImageBytes(100, 100, "jpg");
      MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", bytes);

      assertThatCode(() -> ImageUtil.validateContent(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should throw InvalidInputException when file is not an allowed image type")
    void validateContent_WhenNotImage_ShouldThrowException() {
      byte[] textBytes = "This is a plain text file pretending to be image".getBytes();
      MockMultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", textBytes);

      assertThatThrownBy(() -> ImageUtil.validateContent(file))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Invalid image file type");
    }

    @Test
    @DisplayName("should throw InvalidInputException when image dimensions exceed 4096")
    void validateContent_WhenDimensionsExceedLimit_ShouldThrowException() throws Exception {
      byte[] largeDimensionBytes = createValidImageBytes(4097, 100, "png");
      MockMultipartFile file =
          new MockMultipartFile("file", "oversized.png", "image/png", largeDimensionBytes);

      assertThatThrownBy(() -> ImageUtil.validateContent(file))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Image dimensions");
    }
  }

  @Nested
  @DisplayName("resizeImageToStream(InputStream in, int width, int height)")
  class ResizeImageToStreamTest {

    @Test
    @DisplayName("should resize image and return readable stream")
    void resizeImageToStream_WhenValidInput_ShouldReturnResizedStream() throws Exception {
      byte[] pngBytes = createValidImageBytes(200, 200, "png");
      InputStream in = new ByteArrayInputStream(pngBytes);

      try (InputStream resizedStream = ImageUtil.resizeImageToStream(in, 50, 50)) {
        assertThat(resizedStream).isNotNull();
        byte[] outputBytes = resizedStream.readAllBytes();
        assertThat(outputBytes).isNotEmpty();

        BufferedImage resizedImage = ImageIO.read(new ByteArrayInputStream(outputBytes));
        assertThat(resizedImage).isNotNull();
        assertThat(resizedImage.getWidth()).isEqualTo(50);
        assertThat(resizedImage.getHeight()).isEqualTo(50);
      }
    }
  }
}
