package vn.io.oldmoon.shopizer.user.app.dto.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.business.event.registration.KeycloakUserRegisteredEvent;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPopulator {
  private final UserMapper userMapper;

  public User toUserEntity(KeycloakUserRegisteredEvent event) {
    User user = userMapper.toUserEntity(event);
    log.info(
        "Converted to user from keycloakUserRegisterEvent: userId {}", user.getKeycloakUserId());
    return user;
  }
}
