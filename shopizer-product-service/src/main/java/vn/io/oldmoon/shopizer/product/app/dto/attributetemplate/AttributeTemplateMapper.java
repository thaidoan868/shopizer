package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.io.oldmoon.shopizer.product.app.dto.product.AttributeDto;
import vn.io.oldmoon.shopizer.product.infra.model.product.Attribute;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AttributeTemplateMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  AttributeTemplate toEntity(CreateAttributeTemplateDto dto);

  AttributeTemplateDto toDto(AttributeTemplate template);

  List<AttributeTemplateDto> toDtoList(List<AttributeTemplate> templates);

  Attribute toAttribute(AttributeDto dto);

  AttributeDto toAttributeDto(Attribute attribute);

  List<Attribute> toAttributeList(List<AttributeDto> dtos);

  List<AttributeDto> toAttributeDtoList(List<Attribute> attributes);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updateEntityFromDto(
      UpdateAttributeTemplateDto dto, @MappingTarget AttributeTemplate template);
}
