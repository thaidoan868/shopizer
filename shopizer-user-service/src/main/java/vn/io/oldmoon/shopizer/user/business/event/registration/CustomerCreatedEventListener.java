package vn.io.oldmoon.shopizer.user.business.event.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.ApplicationEventListener;
import vn.io.oldmoon.shopizer.user.app.config.RabbitMqConfig;
import vn.io.oldmoon.shopizer.user.business.service.keycloak.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerCreatedEventListener
    implements ApplicationEventListener<CustomerCreatedEvent> {
  private final KeycloakService keycloakService;

  @Override
  @RabbitListener(queues = RabbitMqConfig.customerCreatedQueue)
  public void handle(CustomerCreatedEvent event) {
    log.info("Processing CustomerCreatedEvent userId={}", event.keycloakUserId());
    keycloakService.assignRealmRole(event.keycloakUserId(), Role.CUSTOMER);
  }
}
