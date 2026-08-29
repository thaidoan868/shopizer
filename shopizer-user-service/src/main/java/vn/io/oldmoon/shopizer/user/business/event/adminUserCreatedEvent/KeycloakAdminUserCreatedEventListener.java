package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminUserCreatedEventListener
    implements ApplicationEventListener<KeycloakAdminUserCreatedEvent> {

  private final KeycloakAdminEventParser eventParser;
  private final UserService userService;

  @Override
  @RabbitListener(queues = RabbitMqConfig.AdminUserCreatedQueue)
  public void handle(KeycloakAdminUserCreatedEvent event) {
    log.info(
        "Processing KeycloakAdminUserCreatedEvent: resourcePath={}, operationType={}, resourceType={}",
        event.resourcePath(),
        event.operationType(),
        event.resourceType());

    User user = eventParser.toUserEntity(event);
    User savedUser = userService.create(user);
    log.info(
        "Successfully synced user from admin create event: keycloakUserId={}, username={}, email={}",
        savedUser.getKeycloakUserId(),
        savedUser.getUsername(),
        savedUser.getEmail());
  }
}
