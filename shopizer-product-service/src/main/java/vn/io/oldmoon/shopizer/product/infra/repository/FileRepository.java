package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

  Optional<File> findByBucketAndObjectName(String bucket, String objectName);
}
