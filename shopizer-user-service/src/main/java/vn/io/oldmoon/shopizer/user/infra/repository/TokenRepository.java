package vn.io.oldmoon.shopizer.user.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;
import vn.io.oldmoon.shopizer.user.infra.model.Token;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    @Query(value = """
            SELECT *
            FROM token
            WHERE email = :email
              AND code = :code
              AND type = :type
              AND expires_at > :now
            ORDER BY expires_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Token> findValidToken(
            @Param("email") String email,
            @Param("code") String code,
            @Param("type") TokenType type,
            @Param("now") Instant now
    );
}