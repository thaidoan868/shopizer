package vn.io.oldmoon.shopizer.product.app.dto.media;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.utility.ConverterUtil;
import vn.io.oldmoon.shopizer.common.core.utility.FileUtil;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilePopulator {

  private final MediaMapper mediaMapper;

  @Value("${minio.access-endpoint}")
  private String mediaEndpoint;

  /**
   * Converts a File domain entity into a FileResponse DTO with its public URL populated.
   *
   * @param file the File domain entity
   * @return the populated FileResponse DTO
   */
  public FileResponse toDto(File file) {
    Objects.requireNonNull(file, "File must not be null");

    FileResponse response = mediaMapper.toDto(file);
    String encodedBucket = FileUtil.encodeForUrl(file.getBucket());
    String encodedObjectName = FileUtil.encodeForUrl(file.getObjectName());
    response.setUrl(ConverterUtil.toMediaUrl(mediaEndpoint, encodedBucket, encodedObjectName));

    log.debug("Mapped File to FileResponse for id={}", file.getId());
    return response;
  }
}
