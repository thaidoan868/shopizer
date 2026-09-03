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

    try {
      String detectedMime;
      try (InputStream mimeStream = new BufferedInputStream(file.getInputStream())) {
        detectedMime = TIKA.detect(mimeStream, file.getOriginalFilename());
      }
      if (detectedMime == null || !ALLOWED_IMAGE_TYPES.contains(detectedMime.toLowerCase())) {
        throw new InvalidInputException(
            "Invalid image file type. Detected format: " + detectedMime);
      }

      BufferedImage img;
      try (InputStream imgStream = new BufferedInputStream(file.getInputStream())) {
        img = ImageIO.read(imgStream);
      }
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

    } catch (InvalidInputException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidInputException(
          "Failed to process image file: " + file.getOriginalFilename());
    }
  }

  public static InputStream resizeImageToStream(InputStream in, int width, int height)
      throws IOException {
    try (in) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Thumbnails.of(in)
          .size(width, height)
          .crop(Positions.CENTER)
          .outputFormat("jpg")
          .toOutputStream(out);
      return new ByteArrayInputStream(out.toByteArray());
    }
  }
}
