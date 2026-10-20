package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;

@Repository
public interface VideoAssetRepository extends JpaRepository<VideoAsset, UUID> {}
