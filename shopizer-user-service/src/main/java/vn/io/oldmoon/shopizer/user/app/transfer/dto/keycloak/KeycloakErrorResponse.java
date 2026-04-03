package vn.io.oldmoon.shopizer.user.app.transfer.dto.keycloak;

import java.util.List;
import lombok.Data;

@Data
public class KeycloakErrorResponse {
  private String errorMessage;
  private String error;
  private String field;
  private List<String> params;
  private List<String> errors;
}
