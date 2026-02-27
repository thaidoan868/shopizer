package vn.io.oldmoon.shopizer.user.bussiness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.user.bussiness.exception.BusinessException;
import vn.io.oldmoon.shopizer.user.bussiness.exception.ErrorCode;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;
import vn.io.oldmoon.shopizer.user.infra.model.Token;
import vn.io.oldmoon.shopizer.user.infra.repository.TokenRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {
    private final TokenRepository tokenRepo;

    @Value("${application.token-expiration}")
    private Duration tokenExpiration;

    public String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    @Transactional
    public Token create(Token token) {
        token.setExpiresAt(Instant.now().plus(tokenExpiration));
        Token createdToken = tokenRepo.save(token);

        log.info("Token created: tokenId={}", createdToken.getId());
        return createdToken;
    }

    public Optional<Token> get(String username, String email, TokenType type) {
        Optional<Token> token = tokenRepo.findValidToken(username, email, type.name(), Instant.now());
        token.ifPresentOrElse(
                token1 -> log.info("Fetched token: code={}, email={}", token1.getCode(), email),
                () -> log.info("Token not found: email={}", email)
        );
        return token;
    }

    public Optional<Token> getTokenByCode(String email, String code, TokenType type) {
        Optional<Token> token = tokenRepo.findTokenByCode(email, code, type.name(), Instant.now());
        token.ifPresentOrElse(
                token1 -> log.info("fetch token: code={}, email={}", token1.getCode(), email),
                () -> log.info("Token not found: email={}", email)
        );
        return token;
    }


    public void expireToken(UUID id) {
        Token token = tokenRepo
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.NOT_FOUND, "Not found token: id=%s".formatted(id))
                );
        token.setExpiresAt(Instant.now());
        tokenRepo.save(token);

        log.info("Token expired: tokenId={}", token.getId());
    }
}
