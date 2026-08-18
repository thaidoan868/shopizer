package vn.io.oldmoon.shopizer.user.business.event.registration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUserRegisteredEvent(UUID userId, KeycloakRegistrationDetails details)
    implements ApplicationEvent {}
