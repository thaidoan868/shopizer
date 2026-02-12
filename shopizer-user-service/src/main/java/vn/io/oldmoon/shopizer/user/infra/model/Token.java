package vn.io.oldmoon.shopizer.user.infra.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @UuidGenerator
    private UUID id;

    @NotNull
    private UUID userId;

    @NotBlank
    private String email;

    @NotNull
    private String code;

    @NotNull
    private Instant expiresAt;

    @CreationTimestamp
    private Instant createdAt;
}
