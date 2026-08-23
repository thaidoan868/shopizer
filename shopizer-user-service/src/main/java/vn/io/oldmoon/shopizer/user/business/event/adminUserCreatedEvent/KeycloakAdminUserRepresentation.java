package vn.io.oldmoon.shopizer.user.business.event.adminUserCreatedEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public record KeycloakAdminUserRepresentation(
    String username,
    String email,
    String firstName,
    String lastName,
    Boolean emailVerified,
    Boolean enabled,
    Map<String, List<String>> attributes,
    List<String> requiredActions,
    List<String> groups) {}
