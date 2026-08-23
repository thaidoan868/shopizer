package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;
import vn.io.oldmoon.shopizer.common.event.ApplicationEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record KeycloakAdminUserCreatedEvent(
    Long time,
    String realmId,
    KeycloakAdminAuthDetails authDetails,
    String resourceType,
    String operationType,
    String resourcePath,
    String representation,
    String resourceTypeAsString)
    implements ApplicationEvent {

  public UUID getExtractedUserId() {
    if (resourcePath == null || resourcePath.isBlank()) {
      return null;
    }
    String path = resourcePath.trim();
    if (path.contains("/")) {
      path = path.substring(path.lastIndexOf('/') + 1);
    }
    try {
      return UUID.fromString(path);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
