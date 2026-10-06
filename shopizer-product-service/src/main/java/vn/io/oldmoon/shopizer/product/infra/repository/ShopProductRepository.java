package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;

public interface ShopProductRepository extends JpaRepository<Product, UUID> {}

