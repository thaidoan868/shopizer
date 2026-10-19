package vn.io.oldmoon.shopizer.product.app.dto.product;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.io.oldmoon.shopizer.product.infra.model.product.Attribute;
import vn.io.oldmoon.shopizer.product.infra.model.product.Product;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

  @Mapping(target = "skus", ignore = true)
  Product toEntity(CreateProductRequest dto);

  CreateProductResponse toDto(Product product);

  List<CreateProductResponse> toDtoList(List<Product> products);

  Attribute toAttribute(AttributeDto dto);

  AttributeDto toAttributeDto(Attribute attribute);

  List<Attribute> toAttributeList(List<AttributeDto> dtos);

  List<AttributeDto> toAttributeDtoList(List<Attribute> attributes);
}
