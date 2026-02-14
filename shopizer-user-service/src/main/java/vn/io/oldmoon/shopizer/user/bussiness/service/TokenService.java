package vn.io.oldmoon.shopizer.user.bussiness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.user.bussiness.exception.BusinessException;
import vn.io.oldmoon.shopizer.user.bussiness.exception.ErrorCode;
import vn.io.oldmoon.shopizer.user.infra.model.Token;
import vn.io.oldmoon.shopizer.user.infra.repository.TokenRepository;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {
    private final TokenRepository tokenRepo;

    public String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    @Transactional
    public Token create(Token token) {
        Token createdToken = tokenRepo.save(token);
        log.info("Token created: tokenId={}", createdToken.getId());
        return createdToken;
    }

    public Token get(String email, String code) {
        Token token = tokenRepo.findFirstByEmailAndCodeAndExpiresAtAfter(email, code, Instant.now()).orElseThrow(() ->
                new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "Token with email '%s' and code '%s' not found'".formatted(email, code)
                )
        );
        log.info("Token fetched: tokenId={}", token.getId());
        return token;
    }
}
