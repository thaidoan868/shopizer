package vn.io.oldmoon.shopizer.user.business.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.infra.model.profile.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPopulator {
  private UserMapper userMapper;

  public User toUserEntity(KeycloakUserRegisterEvent event) {
    User user = userMapper.toUserEntity(event);
    log.info(
        "Converted to user from keycloakUserRegisterEvent: keycloakUserId {}",
        user.getKeycloakUserId());
    return user;
  }
}
