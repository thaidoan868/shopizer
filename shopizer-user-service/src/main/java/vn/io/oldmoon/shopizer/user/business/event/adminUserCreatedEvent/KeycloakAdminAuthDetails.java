package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record KeycloakAdminAuthDetails(
    String realmId, String realmName, String clientId, String userId, String ipAddress) {}
