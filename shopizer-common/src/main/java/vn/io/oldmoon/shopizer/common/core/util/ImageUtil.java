package vn.io.oldmoon.shopizer.common.core.util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;

@Slf4j
public class ImageUtil {
  private static final Tika TIKA = new Tika();

  private static final Set<String> ALLOWED_IMAGE_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "image/bmp");

  private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
  private static final int MAX_WIDTH = 4096;
  private static final int MAX_HEIGHT = 4096;

  public static void validateBasic(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new InvalidInputException("The file is empty.");
    }
    if (file.getSize() > MAX_IMAGE_SIZE) {
      throw new InvalidInputException(
          "Image exceeds maximum size of 5MB: " + file.getOriginalFilename());
    }
  }

  public static void validateContent(MultipartFile file) {
    validateBasic(file);

    try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {

      String detectedMime = TIKA.detect(inputStream, file.getOriginalFilename());
      if (detectedMime == null || !ALLOWED_IMAGE_TYPES.contains(detectedMime.toLowerCase())) {
        throw new InvalidInputException(
            "Invalid image file type. Detected format: " + detectedMime);
      }

      BufferedImage img = ImageIO.read(inputStream);
      if (img == null) {
        throw new InvalidInputException("Cannot decode image: " + file.getOriginalFilename());
      }

      if (img.getWidth() > MAX_WIDTH || img.getHeight() > MAX_HEIGHT) {
        throw new InvalidInputException(
            "Image dimensions (%dx%d) exceed the allowed limit (%dx%d): %s"
                .formatted(
                    img.getWidth(),
                    img.getHeight(),
                    MAX_WIDTH,
                    MAX_HEIGHT,
                    file.getOriginalFilename()));
      }

    } catch (Exception e) {
      throw new InvalidInputException(
          "Failed to process image file: " + file.getOriginalFilename());
    }
  }

  public InputStream resizeImageToStream(MultipartFile file, int width, int height)
      throws IOException {
    PipedInputStream in = new PipedInputStream();
    PipedOutputStream out = new PipedOutputStream(in);

    // Run Thumbnailator processing in a virtual thread / background task
    Thread.ofVirtual()
        .start(
            () -> {
              try (out;
                  InputStream fileIn = file.getInputStream()) {
                Thumbnails.of(fileIn)
                    .size(width, height)
                    .crop(Positions.CENTER)
                    .outputFormat("jpg")
                    .toOutputStream(out);
              } catch (IOException e) {
                log.error("Failed to resize image stream", e);
              }
            });

    return in;
  }
}
