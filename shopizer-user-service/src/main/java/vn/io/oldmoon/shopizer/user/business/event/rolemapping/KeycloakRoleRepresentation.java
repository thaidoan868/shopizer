package vn.io.oldmoon.shopizer.user.business.event.rolemapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public record KeycloakRoleRepresentation(
    String id,
    String name,
    String description,
    Boolean composite,
    Boolean clientRole,
    String containerId) {}
