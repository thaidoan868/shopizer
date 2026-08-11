package vn.io.oldmoon.shopizer.user.business.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUserRegisterEvent(UUID userId, KeycloakRegistrationDetails details)
    implements ApplicationEvent {}
