package vn.io.oldmoon.shopizer.product.app.dto.media;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.product.infra.model.media.Video;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoAssetPopulator {

  private final FilePopulator filePopulator;

  /**
   * Converts a Video domain object into a VideoResponse DTO.
   *
   * @param video the Video domain object
   * @return the populated VideoResponse DTO, or null if input is null
   */
  public VideoResponse toVideoResponse(Video video) {
    if (video == null) {
      return null;
    }

    FileResponse fileResponse =
        video.getFile() != null ? filePopulator.toDto(video.getFile()) : null;

    return VideoResponse.builder().status(video.getStatus()).file(fileResponse).build();
  }

  /**
   * Converts a VideoAsset domain entity into a CreateVideoAssetResponse DTO.
   *
   * @param videoAsset the VideoAsset domain entity
   * @return the populated CreateVideoAssetResponse DTO
   */
  public CreateVideoAssetResponse toDto(VideoAsset videoAsset) {
    Objects.requireNonNull(videoAsset, "VideoAsset must not be null");

    CreateVideoAssetResponse response =
        CreateVideoAssetResponse.builder()
            .id(videoAsset.getId())
            .original(toVideoResponse(videoAsset.getOriginal()))
            .p360(toVideoResponse(videoAsset.getP360()))
            .p720(toVideoResponse(videoAsset.getP720()))
            .p1080(toVideoResponse(videoAsset.getP1080()))
            .build();

    log.debug("Mapped VideoAsset to CreateVideoAssetResponse for id={}", videoAsset.getId());
    return response;
  }

  /**
   * Converts a list of VideoAsset domain entities into a list of CreateVideoAssetResponse DTOs.
   *
   * @param videoAssets the list of VideoAsset entities
   * @return the list of populated CreateVideoAssetResponse DTOs
   */
  public List<CreateVideoAssetResponse> toDtoList(List<VideoAsset> videoAssets) {
    Objects.requireNonNull(videoAssets, "VideoAsset list must not be null");

    List<CreateVideoAssetResponse> responses = videoAssets.stream().map(this::toDto).toList();
    log.debug(
        "Mapped {} VideoAsset entities to CreateVideoAssetResponse list", videoAssets.size());
    return responses;
  }
}
