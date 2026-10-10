package vn.io.oldmoon.shopizer.product.business.service;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.InvalidInputException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;
import vn.io.oldmoon.shopizer.product.infra.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

  private final ProductRepository productRepository;

  /**
   * Creates a new product. If a slug is not provided, generates a default one using the product
   * name, replacing whitespace with hyphens and appending a unique code.
   *
   * @param product the Product domain entity to persist
   * @return the persisted Product domain entity
   */
  @Transactional
  public Product create(Product product) {
    Objects.requireNonNull(product, "Product must not be null");

    log.info("Creating product with name='{}', slug='{}'", product.getName(), product.getSlug());
    return productRepository.save(product);
  }

  /**
   * Retrieves a product by either its UUID or its slug.
   *
   * @param identifier UUID string or slug
   * @return the Product domain entity
   * @throws ResourceNotFoundException if no matching product is found
   */
  public Product getByIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      throw new InvalidInputException("Product identifier must not be blank");
    }

    String sanitizedIdentifier = identifier.trim().toLowerCase();
    Product product;

    UUID id = UUID.fromString(sanitizedIdentifier);
    product =
        productRepository
            .findById(id)
            .or(() -> productRepository.findBySlug(sanitizedIdentifier))
            .or(() -> productRepository.findBySlugIgnoreCase(sanitizedIdentifier))
            .orElseThrow(() -> new ResourceNotFoundException("Product", identifier));

    log.info(
        "Found product id='{}', slug='{}' for identifier='{}'",
        product.getId(),
        product.getSlug(),
        identifier);
    return product;
  }
}
