package vn.io.oldmoon.shopizer.user.app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AvatarDto(
    @Schema(example = "http://localhost:9000/public-assets/avatar-2773-18934-3478392.png")
        String originalAvatarUrl,
    @Schema(example = "http://localhost:9000/public-assets/avatar-medium-2773-18934-3478392.png")
        String mediumAvatarUrl,
    @Schema(example = "http://localhost:9000/public-assets/avatar-thumbnail-2773-18934-3478392.png")
        String thumbnailAvatarUrl) {}
