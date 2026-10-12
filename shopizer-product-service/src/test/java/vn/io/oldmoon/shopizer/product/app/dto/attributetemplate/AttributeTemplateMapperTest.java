package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.io.oldmoon.shopizer.product.infra.model.product.Attribute;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

class AttributeTemplateMapperTest {

  private final AttributeTemplateMapper mapper =
      Mappers.getMapper(AttributeTemplateMapper.class);

  @Test
  @DisplayName("toEntity should return null when dto is null")
  void toEntity_WhenDtoIsNull_ShouldReturnNull() {
    assertThat(mapper.toEntity(null)).isNull();
  }

  @Test
  @DisplayName("toEntity should map valid dto to entity")
  void toEntity_WhenDtoIsValid_ShouldMapToEntity() {
    AttributeRequest attrDto = AttributeRequest.builder().name("Memory").value("16GB").build();
    CreateAttributeTemplateRequest request =
        CreateAttributeTemplateRequest.builder()
            .name("RAM Template")
            .code("RAM_TMPL")
            .attributes(List.of(attrDto))
            .build();

    AttributeTemplate entity = mapper.toEntity(request);

    assertThat(entity).isNotNull();
    assertThat(entity.getName()).isEqualTo("RAM Template");
    assertThat(entity.getCode()).isEqualTo("RAM_TMPL");
    assertThat(entity.getAttributes()).hasSize(1);
    assertThat(entity.getAttributes().getFirst().name()).isEqualTo("Memory");
    assertThat(entity.getAttributes().getFirst().value()).isEqualTo("16GB");
  }

  @Test
  @DisplayName("toDto should return null when template is null")
  void toDto_WhenTemplateIsNull_ShouldReturnNull() {
    assertThat(mapper.toDto(null)).isNull();
  }

  @Test
  @DisplayName("toDto should map valid entity to response")
  void toDto_WhenTemplateIsValid_ShouldMapToDto() {
    UUID id = UUID.randomUUID();
    Attribute attr = new Attribute("Memory", "16GB");
    AttributeTemplate entity =
        AttributeTemplate.builder()
            .name("RAM Template")
            .code("RAM_TMPL")
            .attributes(List.of(attr))
            .build();
    entity.setId(id);

    AttributeTemplateResponse response = mapper.toDto(entity);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getName()).isEqualTo("RAM Template");
    assertThat(response.getCode()).isEqualTo("RAM_TMPL");
    assertThat(response.getAttributes()).hasSize(1);
    assertThat(response.getAttributes().getFirst().getName()).isEqualTo("Memory");
    assertThat(response.getAttributes().getFirst().getValue()).isEqualTo("16GB");
  }

  @Test
  @DisplayName("toShortDtoList should handle null and valid list")
  void toShortDtoList_Tests() {
    assertThat(mapper.toShortDtoList(null)).isNull();

    UUID id = UUID.randomUUID();
    AttributeTemplate entity =
        AttributeTemplate.builder().name("GPU").code("GPU_CODE").build();
    entity.setId(id);
    List<ShortAttributeTemplateResponse> list =
        mapper.toShortDtoList(List.of(entity));

    assertThat(list).isNotNull().hasSize(1);
    assertThat(list.getFirst().getId()).isEqualTo(id);
    assertThat(list.getFirst().getName()).isEqualTo("GPU");
    assertThat(list.getFirst().getCode()).isEqualTo("GPU_CODE");
  }

  @Test
  @DisplayName("updateEntityFromDto should handle null dto and partial updates")
  void updateEntityFromDto_Tests() {
    AttributeTemplate entity =
        AttributeTemplate.builder().name("Old Name").code("OLD_CODE").build();
    UUID id = UUID.randomUUID();
    entity.setId(id);

    // null dto shouldn't modify entity
    mapper.updateEntityFromDto(null, entity);
    assertThat(entity.getName()).isEqualTo("Old Name");

    // partial update
    AttributeRequest attrDto = AttributeRequest.builder().name("Power").value("750W").build();
    UpdateAttributeTemplateRequest request =
        UpdateAttributeTemplateRequest.builder()
            .name("New Name")
            .code("NEW_CODE")
            .attributes(List.of(attrDto))
            .build();

    mapper.updateEntityFromDto(request, entity);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getName()).isEqualTo("New Name");
    assertThat(entity.getCode()).isEqualTo("NEW_CODE");
    assertThat(entity.getAttributes()).hasSize(1);
    assertThat(entity.getAttributes().getFirst().name()).isEqualTo("Power");
  }

  @Test
  @DisplayName("toAttribute should handle null and valid AttributeRequest")
  void toAttribute_Tests() {
    assertThat(mapper.toAttribute(null)).isNull();

    AttributeRequest dto = AttributeRequest.builder().name("Color").value("Black").build();
    Attribute attribute = mapper.toAttribute(dto);
    assertThat(attribute).isNotNull();
    assertThat(attribute.name()).isEqualTo("Color");
    assertThat(attribute.value()).isEqualTo("Black");
  }

  @Test
  @DisplayName("toAttributeRequest should handle null and valid Attribute")
  void toAttributeRequest_Tests() {
    assertThat(mapper.toAttributeRequest(null)).isNull();

    Attribute attribute = new Attribute("Color", "Black");
    AttributeRequest dto = mapper.toAttributeRequest(attribute);
    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isEqualTo("Color");
    assertThat(dto.getValue()).isEqualTo("Black");
  }

  @Test
  @DisplayName("toAttributeList should handle null and valid list")
  void toAttributeList_Tests() {
    assertThat(mapper.toAttributeList(null)).isNull();

    AttributeRequest dto = AttributeRequest.builder().name("Cores").value("2560").build();
    List<Attribute> list = mapper.toAttributeList(List.of(dto));
    assertThat(list).isNotNull().hasSize(1);
    assertThat(list.getFirst().name()).isEqualTo("Cores");
  }

  @Test
  @DisplayName("toAttributeRequestList should handle null and valid list")
  void toAttributeRequestList_Tests() {
    assertThat(mapper.toAttributeRequestList(null)).isNull();

    Attribute attr = new Attribute("Cores", "2560");
    List<AttributeRequest> list = mapper.toAttributeRequestList(List.of(attr));
    assertThat(list).isNotNull().hasSize(1);
    assertThat(list.getFirst().getName()).isEqualTo("Cores");
  }
}
