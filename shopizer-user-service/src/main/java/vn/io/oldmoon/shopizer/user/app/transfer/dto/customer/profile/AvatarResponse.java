package vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile;

import io.swagger.v3.oas.annotations.media.Schema;

public record AvatarResponse(
    @Schema(example = "http://localhost:9000/public-assets/avatar-2773-18934-3478392.png")
        String originalAvatarUrl,
    @Schema(example = "http://localhost:9000/public-assets/avatar-medium-2773-18934-3478392.png")
        String mediumAvatarUrl,
    @Schema(example = "http://localhost:9000/public-assets/avatar-thumbnail-2773-18934-3478392.png")
        String thumbnailAvatarUrl) {}
