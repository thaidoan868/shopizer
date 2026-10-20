package vn.io.oldmoon.shopizer.common.core.utility;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;

@Slf4j
public final class VideoUtil {

  private static final Tika TIKA = new Tika();

  private static final Set<String> ALLOWED_VIDEO_TYPES =
      Set.of(
          "video/mp4",
          "video/webm",
          "video/quicktime",
          "video/x-msvideo",
          "video/x-matroska",
          "video/mpeg");

  private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500MB

  private VideoUtil() {}

  public static void validateBasic(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new InvalidInputException("The video file is empty.");
    }
    if (file.getSize() > MAX_VIDEO_SIZE) {
      throw new InvalidInputException(
          "Video exceeds maximum size of 500MB: " + file.getOriginalFilename());
    }
  }

  public static void validateContent(MultipartFile file) {
    validateBasic(file);

    try {
      String detectedMime;
      try (InputStream mimeStream = new BufferedInputStream(file.getInputStream())) {
        detectedMime = TIKA.detect(mimeStream, file.getOriginalFilename());
      }
      if (detectedMime == null || !ALLOWED_VIDEO_TYPES.contains(detectedMime.toLowerCase())) {
        throw new InvalidInputException(
            "Invalid video file type. Detected format: " + detectedMime);
      }
    } catch (InvalidInputException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidInputException(
          "Failed to process video file: " + file.getOriginalFilename());
    }
  }
}
