package vn.io.oldmoon.shopizer.user.business.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.EventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.event.CustomerCreatedEvent;
import vn.io.oldmoon.shopizer.user.business.event.KeycloakUserRegisterEvent;
import vn.io.oldmoon.shopizer.user.business.event.UserPopulator;
import vn.io.oldmoon.shopizer.user.business.service.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.model.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserRegisterEventListener implements EventListener<KeycloakUserRegisterEvent> {
  private final CustomerProfileService customerProfileService;
  private final UserPopulator userPopulator;
  private final ApplicationEventPublisher localPublisher;

  @Override
  @RabbitListener(queues = RabbitMqConfig.userRegisteredQueue)
  public void handle(KeycloakUserRegisterEvent event) {
    log.info("Processing KeycloakUserRegisterEvent keycloakUserId={}", event.userId());
    User user = userPopulator.toUserEntity(event);
    CustomerProfile customerProfile = customerProfileService.create(user);
    CustomerCreatedEvent customerCreatedEvent =
        new CustomerCreatedEvent(customerProfile.getKeycloakUserId().toString());
    localPublisher.publishEvent(customerCreatedEvent);
  }
}
