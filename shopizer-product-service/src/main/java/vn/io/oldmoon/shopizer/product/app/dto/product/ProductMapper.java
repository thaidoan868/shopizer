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
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "skus", ignore = true)
  Product toEntity(CreateProductRequest dto);

  CreateProductResponse toDto(Product product);

  List<CreateProductResponse> toDtoList(List<Product> products);

  Attribute toAttribute(AttributeDto dto);

  AttributeDto toAttributeDto(Attribute attribute);

  List<Attribute> toAttributeList(List<AttributeDto> dtos);

  List<AttributeDto> toAttributeDtoList(List<Attribute> attributes);
}
