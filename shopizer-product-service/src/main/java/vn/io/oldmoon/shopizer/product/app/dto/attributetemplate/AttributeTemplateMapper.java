package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
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
  AttributeTemplate toEntity(CreateAttributeTemplateRequest dto);

  AttributeTemplateResponse toDto(AttributeTemplate template);

  List<ShortAttributeTemplateResponse> toShortDtoList(List<AttributeTemplate> templates);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updateEntityFromDto(
      UpdateAttributeTemplateRequest dto, @MappingTarget AttributeTemplate template);

  Attribute toAttribute(AttributeRequest dto);

  AttributeRequest toAttributeRequest(Attribute attribute);

  List<Attribute> toAttributeList(List<AttributeRequest> dtos);

  List<AttributeRequest> toAttributeRequestList(List<Attribute> attributes);
}
