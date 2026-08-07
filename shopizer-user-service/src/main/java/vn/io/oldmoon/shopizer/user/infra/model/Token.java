package vn.io.oldmoon.shopizer.user.infra.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import vn.io.oldmoon.shopizer.user.infra.data.constant.TokenType;

@Data
@NoArgsConstructor
@Deprecated
public class Token {

    @Id
    @UuidGenerator
    private UUID id;

    private UUID userId;

    @NotBlank
    private String username;

    @NotBlank
    private String email;

    @NotNull
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @NotNull
    private Instant expiresAt;

    @CreationTimestamp
    private Instant createdAt;

    public Token(String username, String email, String code, TokenType type) {
        this.username = username;
        this.email = email;
        this.code = code;
        this.type = type;
    }
}
