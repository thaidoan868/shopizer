package vn.io.oldmoon.shopizer.user.business.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.user.business.event.CustomerCreatedEvent;
import vn.io.oldmoon.shopizer.user.business.service.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerCreatedEventListener {
  private final KeycloakService keycloakService;

  @EventListener
  @Async
  public void handle(CustomerCreatedEvent event) {
    log.info("Processing CustomerCreatedEvent keycloakUserId={}", event.keycloakUserId());
    keycloakService.assignRealmRole(event.keycloakUserId(), Role.CUSTOMER);
  }
}
