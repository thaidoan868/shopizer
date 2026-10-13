package vn.io.oldmoon.shopizer.product.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

  Optional<Article> findBySlug(String slug);

  Optional<Article> findBySlugIgnoreCase(String slug);

  boolean existsBySlug(String slug);
}
