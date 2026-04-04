package vn.io.oldmoon.shopizer.user.business.listener;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.io.oldmoon.shopizer.common.event.EventListener;
import vn.io.oldmoon.shopizer.common.event.UserCreatedEvent;
import vn.io.oldmoon.shopizer.rabbitmq.RabbitConstants;
import vn.io.oldmoon.shopizer.user.business.service.CustomerService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

// create a profile for the new user
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedCreateProfileListener implements EventListener<UserCreatedEvent> {
  private final CustomerService customerService;

  @Override
  @RabbitListener(queues = RabbitConstants.USER_CREATED_QUEUE)
  public void handle(UserCreatedEvent event) {
    CustomerProfile profile =
        CustomerProfile.builder()
            .userId(UUID.fromString(event.userId()))
            .email(event.email())
            .username(event.username())
            .firstName(event.firstName())
            .lastName(event.lastName())
            .build();
    CustomerProfile savedProfile = customerService.createProfile(profile);
    log.info("Handled UserCreatedEvent: Created profile with id={}", savedProfile.getId());
  }
}
