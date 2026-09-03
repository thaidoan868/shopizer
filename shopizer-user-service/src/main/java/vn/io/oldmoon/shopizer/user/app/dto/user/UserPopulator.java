package vn.io.oldmoon.shopizer.user.app.dto.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPopulator {

  private final UrlConvertService urlConvertService;

  /**
   * Converts an AvatarMeta entity into an AvatarDto.
   *
   * @param avatar the AvatarMeta entity
   */
  public AvatarDto toAvatarDto(AvatarMeta avatar) {
    if (avatar == null) {
      return null;
    }
    AvatarDto avatarDto =
        new AvatarDto(
            urlConvertService.media(avatar.bucket(), avatar.originalObjectName()),
            urlConvertService.media(avatar.bucket(), avatar.mediumObjectName()),
            urlConvertService.media(avatar.bucket(), avatar.thumbnailObjectName()));
    log.info("Converted AvatarMeta to AvatarDto for bucket={}", avatar.bucket());
    return avatarDto;
  }
}
