package vn.io.oldmoon.shopizer.user.business.event.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record KeycloakUpdateEmailDetails(
    String context,
    @JsonProperty("updated_email") String updatedEmail,
    @JsonProperty("previous_email") String previousEmail) {}
