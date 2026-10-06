package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.product.Sku;

@Repository
public interface SkuRepository extends JpaRepository<Sku, UUID> {

  Optional<Sku> findBySkuCode(String skuCode);

  Optional<Sku> findByBarcode(String barcode);
}
