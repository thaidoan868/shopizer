package vn.io.oldmoon.shopizer.user.app.facade;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.user.EmailVerificationCodeRequest;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.user.VerifyEmailRequest;
import vn.io.oldmoon.shopizer.user.business.service.EmailService;
import vn.io.oldmoon.shopizer.user.business.service.KeycloakService;
import vn.io.oldmoon.shopizer.user.business.service.TokenService;
import vn.io.oldmoon.shopizer.user.infra.data.EmailTemplate;
import vn.io.oldmoon.shopizer.user.infra.data.constant.KeycloakRequiredAction;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;
import vn.io.oldmoon.shopizer.user.infra.model.Token;

@Service
@RequiredArgsConstructor
public class UserFacade {
  private final TaskExecutor taskExecutor;

  private final KeycloakService keycloakService;
  private final TokenService tokenService;
  private final EmailService emailService;

  public void sendVerificationCode(EmailVerificationCodeRequest request) {
    // get token
    Optional<Token> token =
        tokenService.get(request.getUsername(), request.getEmail(), TokenType.EMAIL_VERIFICATION);

    if (token.isPresent()) {
      throw new ApiException(
          ErrorCode.CONFLICT,
          "Email verification code already exists for this email: email=" + request.getEmail());
    } else {
      // create new token
      Token newToken =
          new Token(
              request.getUsername(),
              request.getEmail(),
              tokenService.generateCode(),
              TokenType.EMAIL_VERIFICATION);
      tokenService.create(newToken);

      // send an email with the created code
      taskExecutor.execute(
          () ->
              emailService.sendMail(
                  request.getEmail(),
                  EmailTemplate.EMAIL_VERIFICATION_SUBJECT,
                  EmailTemplate.verifyEmail(request.getUsername(), newToken.getCode())));
    }
  }

  public void verifyEmail(VerifyEmailRequest request) {
    // is there any code that matches the request
    Token token =
        tokenService
            .getTokenByCode(request.getEmail(), request.getCode(), TokenType.EMAIL_VERIFICATION)
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.NOT_FOUND,
                        "Not found token: email=%s, code=%s"
                            .formatted(request.getEmail(), request.getCode())));

    // if yes, set email verification to true and delete the required action EMAIL_VERIFY
    UserRepresentation userRep =
        token.getUserId() == null
            ? keycloakService.getUserByUsername(token.getUsername())
            : keycloakService.get(token.getUserId());
    userRep.setEmailVerified(true);
    if (userRep.getRequiredActions() != null) {
      userRep.getRequiredActions().remove(KeycloakRequiredAction.VERIFY_EMAIL.name());
    }

    keycloakService.update(userRep);

    taskExecutor.execute(() -> tokenService.expireToken(token.getId()));
  }
}
