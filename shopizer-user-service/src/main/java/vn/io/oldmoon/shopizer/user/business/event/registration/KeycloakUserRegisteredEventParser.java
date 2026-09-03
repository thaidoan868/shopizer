package vn.io.oldmoon.shopizer.user.business.event.registration;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserRegisteredEventParser {
  private final KeycloakUserRegisteredEventMapper keycloakuserRegisteredEventMapper;

  public User toUserEntity(KeycloakUserRegisteredEvent event) {
    Objects.requireNonNull(event);

    User user = keycloakuserRegisteredEventMapper.toUserEntity(event);
    log.info(
        "Converted to user from keycloakUserRegisterEvent: userId {}", user.getKeycloakUserId());
    return user;
  }
}
