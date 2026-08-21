package vn.io.oldmoon.shopizer.user.business.event.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.business.event.RabbitMqEventPublisher;
import vn.io.oldmoon.shopizer.user.business.service.profile.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserRegisteredEventListener
    implements ApplicationEventListener<KeycloakUserRegisteredEvent> {
  private final CustomerProfileService customerProfileService;
  private final UserPopulator userPopulator;
  private final RabbitMqEventPublisher eventPublisher;

  @Override
  @RabbitListener(queues = RabbitMqConfig.userRegisteredQueue)
  public void handle(KeycloakUserRegisteredEvent event) {
    log.info("Processing KeycloakUserRegisterEvent userId={}", event.userId());
    User user = userPopulator.toUserEntity(event);
    CustomerProfile customerProfile = customerProfileService.create(user);
    CustomerCreatedEvent customerCreatedEvent =
        new CustomerCreatedEvent(customerProfile.getKeycloakUserId().toString());
    log.info("Successfully registered user userId={}", event.userId());
    // I didn't use @Transactional and told Keycloak to assign the customer role directly
    // because, if Keycloak is busy, it could cause database connection pool exhaustion
    eventPublisher.publish(customerCreatedEvent, RabbitMqConfig.customerCreatedBindingKey);
  }
}
