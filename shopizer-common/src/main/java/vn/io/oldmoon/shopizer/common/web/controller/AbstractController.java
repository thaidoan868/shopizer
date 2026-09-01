package vn.io.oldmoon.shopizer.common.web.controller;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import vn.io.oldmoon.shopizer.common.core.exception.AuthenticationException;
import vn.io.oldmoon.shopizer.common.core.util.AuthenticationUtil;
import vn.io.oldmoon.shopizer.common.web.model.UserRepresentation;

@Slf4j
public class AbstractController {
  /**
   * @throws AuthenticationException when the current user is not logged in
   */
  public UserRepresentation getCurrentUser() {
    return AuthenticationUtil.getCurrentUser()
        .orElseThrow(
            () ->
                new AuthenticationException(
                    "Trying to get the current user representation but there is no authentication provided"));
  }

  /**
   * @throws AuthenticationException when the current user is not logged in
   */
  public UUID getCurrentUserId() {
    return AuthenticationUtil.getCurrentUserId()
        .orElseThrow(
            () ->
                new AuthenticationException(
                    "Trying to get the current user id but there is no authentication provided"));
  }
}
