package vn.io.oldmoon.shopizer.product.app.dto.product;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;
import vn.io.oldmoon.shopizer.product.infra.model.product.ProductStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPopulator {

  private final ProductMapper productMapper;

  /**
   * Converts CreateProductDto to a Product domain entity. If a slug is not provided, generates a
   * default one automatically using the product name, replacing whitespace with hyphens and
   * appending a unique code.
   *
   * @param dto the incoming CreateProductDto
   * @return the populated Product domain entity
   */
  public Product toEntity(CreateProductDto dto) {
    Objects.requireNonNull(dto);

    Product product = productMapper.toEntity(dto);

    if (product.getSlug() != null && !product.getSlug().isBlank()) {
      product.setSlug(dto.getSlug().trim().toLowerCase());
    }

    if (product.getSlug() == null || product.getSlug().isBlank()) {
      if (dto.getName() != null && !dto.getName().isBlank()) {
        String base = dto.getName().trim().toLowerCase().replaceAll("\\s+", "-");
        String uniqueCode = UUID.randomUUID().toString().substring(0, 8);
        product.setSlug(base + "-" + uniqueCode);
      }
    }

    if (product.getStatus() == null) {
      product.setStatus(ProductStatus.ACTIVE);
    }
    if (product.getSerialized() == null) {
      product.setSerialized(false);
    }

    log.debug("Mapped CreateProductDto to Product for name={}", product.getName());

    return product;
  }

  /**
   * Converts a Product domain entity to a ProductDto.
   *
   * @param product the Product domain entity
   * @return the corresponding ProductDto
   */
  public ProductDto toDto(Product product) {
    Objects.requireNonNull(product);

    ProductDto dto = productMapper.toDto(product);
    log.debug("Mapped Product to ProductDto: id='{}'", product.getId());
    return dto;
  }

  /**
   * Converts a list of Product domain entities to a list of ProductDto.
   *
   * @param products the list of Product domain entities
   * @return the list of corresponding ProductDto
   */
  public List<ProductDto> toDtoList(List<Product> products) {
    Objects.requireNonNull(products);

    List<ProductDto> dtos = productMapper.toDtoList(products);
    log.debug("Mapped {} Product entities to ProductDto list", products.size());
    return dtos;
  }
}
