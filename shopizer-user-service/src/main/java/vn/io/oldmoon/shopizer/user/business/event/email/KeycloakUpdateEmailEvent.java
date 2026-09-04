package vn.io.oldmoon.shopizer.user.business.event.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;
import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record KeycloakUpdateEmailEvent(
    Long time,
    String type,
    String realmId,
    String clientId,
    UUID userId,
    String ipAddress,
    KeycloakUpdateEmailDetails details)
    implements ApplicationEvent {}
