package vn.io.oldmoon.shopizer.product.app.dto.media;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.product.infra.model.media.ImageAsset;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageAssetPopulator {

  private final FilePopulator filePopulator;

  /**
   * Converts an ImageAsset domain entity into a CreateImageAssetResponse DTO.
   *
   * @param imageAsset the ImageAsset domain entity
   * @return the populated CreateImageAssetResponse DTO
   */
  public CreateImageAssetResponse toDto(ImageAsset imageAsset) {
    Objects.requireNonNull(imageAsset, "ImageAsset must not be null");

    CreateImageAssetResponse response =
        CreateImageAssetResponse.builder()
            .id(imageAsset.getId())
            .original(filePopulator.toDto(imageAsset.getOriginal()))
            .thumbnail(filePopulator.toDto(imageAsset.getThumbnail()))
            .medium(filePopulator.toDto(imageAsset.getMedium()))
            .large(filePopulator.toDto(imageAsset.getLarge()))
            .build();

    log.debug("Mapped ImageAsset to CreateImageAssetResponse for id={}", imageAsset.getId());
    return response;
  }
}
