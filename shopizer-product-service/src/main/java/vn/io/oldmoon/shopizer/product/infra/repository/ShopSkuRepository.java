package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.oldmoon.shopizer.product.infra.model.product.Sku;

public interface ShopSkuRepository extends JpaRepository<Sku, UUID> {}

