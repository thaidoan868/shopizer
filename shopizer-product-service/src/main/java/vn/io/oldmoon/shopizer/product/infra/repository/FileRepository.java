package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.FileStatus;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

  @Modifying
  @Query("UPDATE File f SET f.status = :status WHERE f.id IN :ids")
  void updateStatusByIds(@Param("ids") List<UUID> ids, @Param("status") FileStatus status);
}
