package vn.io.oldmoon.shopizer.common.core.utility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

  /**
   * Extracts the duration in seconds of a video file on disk using ffprobe.
   *
   * @param videoPath the path to the video file
   * @return the duration in seconds rounded to 2 decimal places, or 0.0 if unavailable
   */
  public static double extractDuration(Path videoPath) {
    Objects.requireNonNull(videoPath, "Video path must not be null");

    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "ffprobe",
            "-v",
            "error",
            "-show_entries",
            "format=duration",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            videoPath.toAbsolutePath().toString());
    processBuilder.redirectErrorStream(true);

    try {
      Process process = processBuilder.start();
      String output;
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        output = reader.lines().collect(Collectors.joining()).trim();
      }
      int exitCode = process.waitFor();
      if (exitCode != 0 || output.isEmpty() || "N/A".equalsIgnoreCase(output)) {
        log.warn(
            "ffprobe failed or returned N/A for path={}, exitCode={}, output={}",
            videoPath,
            exitCode,
            output);
        return 0.0;
      }
      double duration = Double.parseDouble(output);
      return Math.max(0.0, Math.round(duration * 100.0) / 100.0);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while extracting video duration for path={}", videoPath, e);
      return 0.0;
    } catch (Exception e) {
      log.warn("Failed to extract video duration for path={}", videoPath, e);
      return 0.0;
    }
  }

  /**
   * Extracts the duration in seconds of an uploaded MultipartFile video using ffprobe.
   *
   * @param file the uploaded video file
   * @return the duration in seconds rounded to 2 decimal places, or 0.0 if unavailable
   */
  public static double extractDuration(MultipartFile file) {
    Objects.requireNonNull(file, "MultipartFile must not be null");
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile("video-probe-", ".tmp");
      try (InputStream in = file.getInputStream()) {
        Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
      }
      return extractDuration(tempFile);
    } catch (Exception e) {
      log.warn("Failed to extract duration from uploaded file: {}", file.getOriginalFilename(), e);
      return 0.0;
    } finally {
      FileUtil.deleteTempFileQuietly(tempFile);
    }
  }
}
