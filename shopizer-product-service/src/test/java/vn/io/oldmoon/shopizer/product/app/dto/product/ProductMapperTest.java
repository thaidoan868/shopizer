package vn.io.oldmoon.shopizer.product.app.dto.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.product.infra.model.product.Attribute;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;
import vn.io.oldmoon.shopizer.product.infra.model.product.ProductStatus;

class ProductMapperTest {

  private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

  @Test
  @DisplayName("toEntity should return null when dto is null")
  void toEntity_WhenDtoIsNull_ShouldReturnNull() {
    assertThat(productMapper.toEntity(null)).isNull();
  }

  @Test
  @DisplayName("toEntity should map valid dto to entity")
  void toEntity_WhenDtoIsValid_ShouldMapToEntity() {
    AttributeDto attrDto = AttributeDto.builder().name("VRAM").value("8GB").build();
    CreateProductRequest request =
        CreateProductRequest.builder()
            .name("RTX 3050")
            .slug("rtx-3050")
            .brand("NVIDIA")
            .description("Budget GPU")
            .attributes(List.of(attrDto))
            .serialized(true)
            .status(ProductStatus.ACTIVE)
            .build();

    Product product = productMapper.toEntity(request);

    assertThat(product).isNotNull();
    assertThat(product.getName()).isEqualTo("RTX 3050");
    assertThat(product.getSlug()).isEqualTo("rtx-3050");
    assertThat(product.getBrand()).isEqualTo("NVIDIA");
    assertThat(product.getDescription()).isEqualTo("Budget GPU");
    assertThat(product.getSerialized()).isTrue();
    assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    assertThat(product.getAttributes()).hasSize(1);
    assertThat(product.getAttributes().getFirst().name()).isEqualTo("VRAM");
    assertThat(product.getAttributes().getFirst().value()).isEqualTo("8GB");
  }

  @Test
  @DisplayName("toDto should return null when product is null")
  void toDto_WhenProductIsNull_ShouldReturnNull() {
    assertThat(productMapper.toDto(null)).isNull();
  }

  @Test
  @DisplayName("toDto should map valid product to response")
  void toDto_WhenProductIsValid_ShouldMapToDto() {
    UUID id = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();
    UUID modifiedBy = UUID.randomUUID();
    Instant now = Instant.now();
    Attribute attr = new Attribute("VRAM", "8GB");

    Product product =
        Product.builder()
            .name("RTX 3050")
            .slug("rtx-3050")
            .brand("NVIDIA")
            .description("Budget GPU")
            .attributes(List.of(attr))
            .serialized(false)
            .status(ProductStatus.ACTIVE)
            .build();
    product.setId(id);
    product.setCreated(now);
    product.setCreatedBy(createdBy);
    product.setModified(now);
    product.setModifiedBy(modifiedBy);

    CreateProductResponse response = productMapper.toDto(product);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getName()).isEqualTo("RTX 3050");
    assertThat(response.getSlug()).isEqualTo("rtx-3050");
    assertThat(response.getBrand()).isEqualTo("NVIDIA");
    assertThat(response.getDescription()).isEqualTo("Budget GPU");
    assertThat(response.getSerialized()).isFalse();
    assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    assertThat(response.getCreated()).isEqualTo(now);
    assertThat(response.getCreatedBy()).isEqualTo(createdBy);
    assertThat(response.getModified()).isEqualTo(now);
    assertThat(response.getModifiedBy()).isEqualTo(modifiedBy);
    assertThat(response.getAttributes()).hasSize(1);
    assertThat(response.getAttributes().getFirst().getName()).isEqualTo("VRAM");
    assertThat(response.getAttributes().getFirst().getValue()).isEqualTo("8GB");
  }

  @Test
  @DisplayName("toDtoList should handle null and valid lists")
  void toDtoList_Tests() {
    assertThat(productMapper.toDtoList(null)).isNull();

    Product product = Product.builder().name("RTX 3050").build();
    List<CreateProductResponse> dtos = productMapper.toDtoList(List.of(product));
    assertThat(dtos).isNotNull().hasSize(1);
    assertThat(dtos.getFirst().getName()).isEqualTo("RTX 3050");
  }

  @Test
  @DisplayName("toAttribute should handle null and valid AttributeDto")
  void toAttribute_Tests() {
    assertThat(productMapper.toAttribute(null)).isNull();

    AttributeDto dto = AttributeDto.builder().name("Color").value("Black").build();
    Attribute attribute = productMapper.toAttribute(dto);

    assertThat(attribute).isNotNull();
    assertThat(attribute.name()).isEqualTo("Color");
    assertThat(attribute.value()).isEqualTo("Black");
  }

  @Test
  @DisplayName("toAttributeDto should handle null and valid Attribute")
  void toAttributeDto_Tests() {
    assertThat(productMapper.toAttributeDto(null)).isNull();

    Attribute attribute = new Attribute("Color", "Black");
    AttributeDto dto = productMapper.toAttributeDto(attribute);

    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isEqualTo("Color");
    assertThat(dto.getValue()).isEqualTo("Black");
  }

  @Test
  @DisplayName("toAttributeList should handle null and valid list")
  void toAttributeList_Tests() {
    assertThat(productMapper.toAttributeList(null)).isNull();

    AttributeDto dto = AttributeDto.builder().name("Cores").value("2560").build();
    List<Attribute> list = productMapper.toAttributeList(List.of(dto));

    assertThat(list).isNotNull().hasSize(1);
    assertThat(list.getFirst().name()).isEqualTo("Cores");
    assertThat(list.getFirst().value()).isEqualTo("2560");
  }

  @Test
  @DisplayName("toAttributeDtoList should handle null and valid list")
  void toAttributeDtoList_Tests() {
    assertThat(productMapper.toAttributeDtoList(null)).isNull();

    Attribute attr = new Attribute("Cores", "2560");
    List<AttributeDto> list = productMapper.toAttributeDtoList(List.of(attr));

    assertThat(list).isNotNull().hasSize(1);
    assertThat(list.getFirst().getName()).isEqualTo("Cores");
    assertThat(list.getFirst().getValue()).isEqualTo("2560");
  }
}
