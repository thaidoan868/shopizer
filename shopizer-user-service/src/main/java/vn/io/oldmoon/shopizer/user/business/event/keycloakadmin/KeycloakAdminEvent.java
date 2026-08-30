package vn.io.oldmoon.shopizer.user.business.event.keycloakadmin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record KeycloakAdminEvent(
    Long time,
    String realmId,
    KeycloakAdminAuthDetails authDetails,
    String resourceType,
    String operationType,
    String resourcePath,
    String representation,
    String resourceTypeAsString)
    implements ApplicationEvent {}
