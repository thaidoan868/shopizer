package vn.io.oldmoon.shopizer.user.business.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.io.oldmoon.shopizer.common.event.EventListener;
import vn.io.oldmoon.shopizer.user.business.event.CustomerCreatedEvent;
import vn.io.oldmoon.shopizer.user.business.service.KeycloakService;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Role;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerCreatedEventListener implements EventListener<CustomerCreatedEvent> {
  private final KeycloakService keycloakService;

  @Override
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(CustomerCreatedEvent event) {
    log.info("Processing CustomerCreatedEvent keycloakUserId={}", event.keycloakUserId());
    keycloakService.assignRealmRole(event.keycloakUserId(), Role.CUSTOMER);
  }
}
