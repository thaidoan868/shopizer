package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.user.app.dto.user.EmailVerificationCodeRequest;
import vn.io.oldmoon.shopizer.user.app.dto.user.VerifyEmailRequest;
import vn.io.oldmoon.shopizer.user.app.facade.UserFacade;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management Endpoints")
public class UserManagementController {
  private final UserFacade userFacade;

  @PostMapping("/email/send-verification-code")
  @Operation(summary = "Send an email verification code if the database doesn't have any valid one")
  public ResponseEntity<?> sendEmailVerificationCode(
      @Valid @RequestBody EmailVerificationCodeRequest request) {
    userFacade.sendVerificationCode(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/email/verify")
  @Operation(summary = "Verify the code against the database")
  public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    userFacade.verifyEmail(request);
    return ResponseEntity.ok().build();
  }

  // @PostMapping("/password/reset");

  // @PostMapping("/password/send-reset-code");

  // @PostMapping("/password/verify-reset-code");

  // @PostMapping("/logout");
}
