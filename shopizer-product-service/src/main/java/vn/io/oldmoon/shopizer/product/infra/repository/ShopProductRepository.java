package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopProductRepository extends JpaRepository<ShopProduct, UUID> {}
