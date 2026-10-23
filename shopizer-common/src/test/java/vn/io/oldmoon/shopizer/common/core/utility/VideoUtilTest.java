package vn.io.oldmoon.shopizer.common.core.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;

class VideoUtilTest {

  @Nested
  @DisplayName("validateBasic(MultipartFile file)")
  class ValidateBasicTests {

    @Test
    @DisplayName("should throw InvalidInputException when file is null")
    void validateBasic_WhenFileIsNull_ShouldThrowException() {
      assertThatThrownBy(() -> VideoUtil.validateBasic(null))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("The video file is empty.");
    }

    @Test
    @DisplayName("should throw InvalidInputException when file is empty")
    void validateBasic_WhenFileIsEmpty_ShouldThrowException() {
      MockMultipartFile emptyFile =
          new MockMultipartFile("video", "empty.mp4", "video/mp4", new byte[0]);
      assertThatThrownBy(() -> VideoUtil.validateBasic(emptyFile))
          .isInstanceOf(InvalidInputException.class)
          .hasMessage("The video file is empty.");
    }

    @Test
    @DisplayName("should pass when file is non-empty and within 500MB limit")
    void validateBasic_WhenValid_ShouldPass() {
      MockMultipartFile validFile =
          new MockMultipartFile("video", "clip.mp4", "video/mp4", new byte[1024]);
      assertThatCode(() -> VideoUtil.validateBasic(validFile)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("validateContent(MultipartFile file)")
  class ValidateContentTests {

    @Test
    @DisplayName("should throw InvalidInputException when file is plain text")
    void validateContent_WhenNotVideo_ShouldThrowException() {
      MockMultipartFile textFile =
          new MockMultipartFile(
              "video", "fake.txt", "text/plain", "not a video file content".getBytes());

      assertThatThrownBy(() -> VideoUtil.validateContent(textFile))
          .isInstanceOf(InvalidInputException.class)
          .hasMessageContaining("Invalid video file type");
    }

    @Test
    @DisplayName("should pass for valid MP4 header bytes")
    void validateContent_WhenMp4Header_ShouldPass() {
      // ISO Base Media file (MP4) ftyp box header
      byte[] mp4Header =
          new byte[] {
            0x00, 0x00, 0x00, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2',
            0x00, 0x00, 0x00, 0x00, 'm', 'p', '4', '2', 'i', 's', 'o', 'm'
          };
      MockMultipartFile mp4File =
          new MockMultipartFile("video", "sample.mp4", "video/mp4", mp4Header);

      assertThatCode(() -> VideoUtil.validateContent(mp4File)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("extractDuration")
  class ExtractDurationTests {

    @Test
    @DisplayName("should throw NullPointerException when path or file is null")
    void extractDuration_WhenNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> VideoUtil.extractDuration((Path) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Video path must not be null");

      assertThatThrownBy(() -> VideoUtil.extractDuration((MultipartFile) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("MultipartFile must not be null");
    }

    @Test
    @DisplayName("should return 0.0 when file cannot be probed")
    void extractDuration_WhenInvalidVideo_ShouldReturnZero() {
      MockMultipartFile invalidVideo =
          new MockMultipartFile("video", "invalid.mp4", "video/mp4", "invalid".getBytes());
      assertThat(VideoUtil.extractDuration(invalidVideo)).isEqualTo(0.0);
    }
  }
}
