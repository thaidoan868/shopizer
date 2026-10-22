package vn.io.oldmoon.shopizer.product.infra.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoAsset;

@Repository
public interface VideoAssetRepository extends JpaRepository<VideoAsset, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM VideoAsset v WHERE v.id = :id")
  Optional<VideoAsset> findByIdForUpdate(@Param("id") UUID id);
}
