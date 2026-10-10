package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeTemplatePopulator {

  private final AttributeTemplateMapper attributeTemplateMapper;

  /**
   * Converts CreateAttributeTemplateDto to an AttributeTemplate domain entity.
   *
   * @param dto the incoming CreateAttributeTemplateDto
   * @return the populated AttributeTemplate domain entity
   */
  public AttributeTemplate toEntity(CreateAttributeTemplateDto dto) {
    Objects.requireNonNull(dto);

    AttributeTemplate template = attributeTemplateMapper.toEntity(dto);
    log.debug("Mapped CreateAttributeTemplateDto to AttributeTemplate for code={}", template.getCode());
    return template;
  }

  /**
   * Updates an existing AttributeTemplate domain entity from UpdateAttributeTemplateDto.
   *
   * @param template the target AttributeTemplate domain entity
   * @param dto the incoming update DTO
   */
  public void update(AttributeTemplate template, UpdateAttributeTemplateDto dto) {
    Objects.requireNonNull(template, "AttributeTemplate must not be null");
    Objects.requireNonNull(dto, "UpdateAttributeTemplateDto must not be null");

    attributeTemplateMapper.updateEntityFromDto(dto, template);
    log.debug("Updated AttributeTemplate for id={}", template.getId());
  }

  /**
   * Converts an AttributeTemplate domain entity to an AttributeTemplateDto.
   *
   * @param template the AttributeTemplate domain entity
   * @return the corresponding AttributeTemplateDto
   */
  public AttributeTemplateDto toDto(AttributeTemplate template) {
    Objects.requireNonNull(template);

    AttributeTemplateDto dto = attributeTemplateMapper.toDto(template);
    log.debug("Mapped AttributeTemplate to AttributeTemplateDto for id={}", template.getId());
    return dto;
  }

  /**
   * Converts a list of AttributeTemplate domain entities to a list of AttributeTemplateDto.
   *
   * @param templates the list of AttributeTemplate domain entities
   * @return the list of corresponding AttributeTemplateDto
   */
  public List<AttributeTemplateDto> toDtoList(List<AttributeTemplate> templates) {
    Objects.requireNonNull(templates);

    List<AttributeTemplateDto> dtos = attributeTemplateMapper.toDtoList(templates);
    log.debug("Mapped AttributeTemplate list to AttributeTemplateDto list for size={}", templates.size());
    return dtos;
  }
}
