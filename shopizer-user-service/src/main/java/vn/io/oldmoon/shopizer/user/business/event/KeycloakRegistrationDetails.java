package vn.io.oldmoon.shopizer.user.business.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakRegistrationDetails(
    @JsonProperty("auth_method") String authMethod,
    @JsonProperty("auth_type") String authType,
    @JsonProperty("register_method") String registerMethod,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("redirect_uri") String redirectUri,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("code_id") String codeId,
    String email,
    String username) {}
