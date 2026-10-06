package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

  Optional<Product> findBySlug(String slug);

  Optional<Product> findBySlugIgnoreCase(String slug);

  boolean existsBySlug(String slug);
}
