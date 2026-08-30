package vn.io.oldmoon.shopizer.user.app.dto.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerMapper;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;
import vn.io.oldmoon.shopizer.user.infra.model.profile.AvatarMeta;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPopulator {

  private final CustomerMapper customerMapper;
  private final UrlConvertService urlConvertService;

  public AvatarDto toAvatarDto(AvatarMeta avatar) {
    if (avatar == null) {
      return null;
    }
    return new AvatarDto(
        urlConvertService.media(avatar.bucket(), avatar.originalObjectName()),
        urlConvertService.media(avatar.bucket(), avatar.mediumObjectName()),
        urlConvertService.media(avatar.bucket(), avatar.thumbnailObjectName()));
  }
}
