package vn.io.oldmoon.shopizer.user.business.event.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record KeycloakVerifyEmailDetails(
    @JsonProperty("auth_method") String authMethod,
    @JsonProperty("token_id") String tokenId,
    String action,
    @JsonProperty("response_type") String responseType,
    @JsonProperty("redirect_uri") String redirectUri,
    @JsonProperty("remember_me") String rememberMe,
    @JsonProperty("code_id") String codeId,
    String email,
    @JsonProperty("response_mode") String responseMode,
    String username) {}
