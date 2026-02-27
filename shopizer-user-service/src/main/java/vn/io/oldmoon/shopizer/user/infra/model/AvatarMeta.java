package vn.io.oldmoon.shopizer.user.infra.model;

public record AvatarMeta(
        String bucket,
        String originalObjectName,
        String mediumObjectName,
        String thumbnailObjectName
) {
}