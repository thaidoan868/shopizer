package vn.io.oldmoon.shopizer.user.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.user.infra.model.FileMeta;

@Repository
public interface FileMetaRepository extends JpaRepository<FileMeta, UUID> {
  Optional<FileMeta> findByBucketAndObjectName(String bucket, String objectName);
}
