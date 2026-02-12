package vn.io.oldmoon.shopizer.user.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.oldmoon.shopizer.user.infra.model.Token;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findFirstByEmailAndCodeAndExpiresAtAfter(String email, String code, Instant now);
}