package vn.io.oldmoon.shopizer.user.bussiness.service.keycloak;

import lombok.Data;

import java.util.List;

@Data
public class KeycloakErrorResponse {
    private String errorMessage;
    private String error;
    private String field;
    private List<String> params;
    private List<String> errors;
}