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
  public AttributeTemplate toEntity(CreateAttributeTemplateRequest dto) {
    Objects.requireNonNull(dto);

    AttributeTemplate template = attributeTemplateMapper.toEntity(dto);
    log.debug(
        "Mapped CreateAttributeTemplateDto to AttributeTemplate for code={}", template.getCode());
    return template;
  }

  /**
   * Updates an existing AttributeTemplate domain entity from UpdateAttributeTemplateDto.
   *
   * @param template the target AttributeTemplate domain entity
   * @param dto the incoming update DTO
   */
  public void update(AttributeTemplate template, UpdateAttributeTemplateRequest dto) {
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
  public AttributeTemplateResponse toDto(AttributeTemplate template) {
    Objects.requireNonNull(template);

    AttributeTemplateResponse dto = attributeTemplateMapper.toDto(template);
    log.debug("Mapped AttributeTemplate to AttributeTemplateDto for id={}", template.getId());
    return dto;
  }

  /**
   * Converts a list of AttributeTemplate domain entities to a ListAttributeTemplateResponse.
   *
   * @param templates the list of AttributeTemplate domain entities
   * @return the ListAttributeTemplateResponse containing short DTOs and total count
   */
  public ListAttributeTemplateResponse toListResponse(List<AttributeTemplate> templates) {
    Objects.requireNonNull(templates);

    List<ShortAttributeTemplateResponse> shortDtos =
        attributeTemplateMapper.toShortDtoList(templates);
    log.debug(
        "Mapped AttributeTemplate list to ListAttributeTemplateResponse for size={}",
        templates.size());
    return ListAttributeTemplateResponse.builder()
        .templates(shortDtos)
        .totalCount(templates.size())
        .build();
  }
}
