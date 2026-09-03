package vn.io.oldmoon.shopizer.user.app.dto.user;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.business.service.UrlConvertService;
import vn.io.oldmoon.shopizer.user.infra.model.user.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPopulator {

  private final UserMapper userMapper;
  private final UrlConvertService urlConvertService;

  /**
   * Converts a User entity into a UserDto using UserMapper.
   *
   * @param user the User entity
   * @return the corresponding UserDto
   */
  public UserDto toUserDto(User user) {
    Objects.requireNonNull(user);

    UserDto userDto = userMapper.toUserDto(user);
    if (user.getAvatarMeta() != null) {
      userDto.setAvatarMeta(toAvatarDto(user.getAvatarMeta()));
    }
    log.info("Converted User to UserDto for keycloakUserId={}", user.getKeycloakUserId());
    return userDto;
  }

  /**
   * Converts an AvatarMeta entity into an AvatarDto.
   *
   * @param avatar the AvatarMeta entity
   * @return the corresponding AvatarDto
   */
  public AvatarDto toAvatarDto(AvatarMeta avatar) {
    Objects.requireNonNull(avatar);

    AvatarDto avatarDto =
        new AvatarDto(
            urlConvertService.media(avatar.bucket(), avatar.originalObjectName()),
            urlConvertService.media(avatar.bucket(), avatar.mediumObjectName()),
            urlConvertService.media(avatar.bucket(), avatar.thumbnailObjectName()));
    log.info(
        "Converted AvatarMeta to AvatarDto for bucket={}, originalObjectName={}",
        avatar.bucket(),
        avatar.originalObjectName());
    return avatarDto;
  }
}
