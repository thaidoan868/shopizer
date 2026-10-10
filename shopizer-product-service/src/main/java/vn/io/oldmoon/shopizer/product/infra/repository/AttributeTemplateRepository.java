package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

@Repository
public interface AttributeTemplateRepository extends JpaRepository<AttributeTemplate, UUID> {

  Optional<AttributeTemplate> findByCode(String code);

  boolean existsByCode(String code);
}
