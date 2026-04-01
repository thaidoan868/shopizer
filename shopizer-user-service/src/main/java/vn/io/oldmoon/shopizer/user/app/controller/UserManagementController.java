package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.user.app.facade.UserFacade;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management Endpoints")
public class UserManagementController {
  private final UserFacade userFacade;

  // @PostMapping("/password/reset");

  // @PostMapping("/password/send-reset-code");

  // @PostMapping("/password/verify-reset-code");

  // @PostMapping("/logout");
}
