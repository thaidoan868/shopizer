// package vn.io.oldmoon.shopizer.user.infra.repository;
//
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import vn.io.oldmoon.shopizer.user.infra.model.Token;
//
// import java.time.Instant;
// import java.util.Optional;
// import java.util.UUID;
//
// public interface TokenRepository extends JpaRepository<Token, UUID> {
//    @Query(value = """
//            SELECT *
//            FROM token
//            WHERE email = :email
//              AND username = :username
//              AND type = :type
//              AND expires_at > :now
//            ORDER BY expires_at DESC
//            LIMIT 1
//            """, nativeQuery = true)
//    Optional<Token> findValidToken(
//            @Param("username") String username,
//            @Param("email") String email,
//            @Param("type") String type,
//            @Param("now") Instant now
//    );
//
//    @Query(value = """
//            SELECT *
//            FROM token
//            WHERE email = :email
//              AND code = :code
//              AND type = :type
//              AND expires_at > :now
//            ORDER BY expires_at DESC
//            LIMIT 1
//            """, nativeQuery = true)
//    Optional<Token> findTokenByCode(
//            @Param("email") String email,
//            @Param("code") String code,
//            @Param("type") String type,
//            @Param("now") Instant now
//    );
//
//    Optional<Token> findById(UUID id);
// }
