package vn.io.oldmoon.shopizer.user.infra.model.profile;

import jakarta.validation.constraints.NotBlank;

public record AvatarMeta(
    @NotBlank String bucket,
    @NotBlank String originalObjectName,
    @NotBlank String mediumObjectName,
    @NotBlank String thumbnailObjectName) {}
