package vn.io.oldmoon.shopizer.product.app.dto.product;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.common.core.utility.SlugUtil;
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
  public Product toEntity(CreateProductRequest dto) {
    Objects.requireNonNull(dto);

    Product product = productMapper.toEntity(dto);

    product.setSlug(SlugUtil.generateSlug(dto.getSlug(), dto.getName()));

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
  public CreateProductResponse toDto(Product product) {
    Objects.requireNonNull(product);

    CreateProductResponse dto = productMapper.toDto(product);
    log.debug("Mapped Product to ProductDto: id='{}'", product.getId());
    return dto;
  }

  /**
   * Converts a list of Product domain entities to a list of ProductDto.
   *
   * @param products the list of Product domain entities
   * @return the list of corresponding ProductDto
   */
  public List<CreateProductResponse> toDtoList(List<Product> products) {
    Objects.requireNonNull(products);

    List<CreateProductResponse> dtos = productMapper.toDtoList(products);
    log.debug("Mapped {} Product entities to ProductDto list", products.size());
    return dtos;
  }
}
