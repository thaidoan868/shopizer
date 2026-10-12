package vn.io.oldmoon.shopizer.product.app.dto.attributetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;

@ExtendWith(MockitoExtension.class)
class AttributeTemplatePopulatorTest {

  @Mock private AttributeTemplateMapper attributeTemplateMapper;

  @InjectMocks private AttributeTemplatePopulator attributeTemplatePopulator;

  @Nested
  @DisplayName("toEntity(CreateAttributeTemplateRequest dto)")
  class ToEntityTests {

    @Test
    @DisplayName("should throw NullPointerException when dto is null")
    void toEntity_WhenDtoIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> attributeTemplatePopulator.toEntity(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should map dto to entity")
    void toEntity_WhenDtoIsValid_ShouldReturnEntity() {
      CreateAttributeTemplateRequest request =
          CreateAttributeTemplateRequest.builder().name("GPU").code("GPU_CODE").build();
      AttributeTemplate expected =
          AttributeTemplate.builder().name("GPU").code("GPU_CODE").build();

      when(attributeTemplateMapper.toEntity(request)).thenReturn(expected);

      AttributeTemplate result = attributeTemplatePopulator.toEntity(request);

      assertThat(result).isNotNull().isEqualTo(expected);
      verify(attributeTemplateMapper).toEntity(request);
    }
  }

  @Nested
  @DisplayName("update(AttributeTemplate template, UpdateAttributeTemplateRequest dto)")
  class UpdateTests {

    @Test
    @DisplayName("should throw NullPointerException when template is null")
    void update_WhenTemplateIsNull_ShouldThrowNpe() {
      UpdateAttributeTemplateRequest request =
          UpdateAttributeTemplateRequest.builder().name("Updated").build();
      assertThatThrownBy(() -> attributeTemplatePopulator.update(null, request))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("AttributeTemplate must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when dto is null")
    void update_WhenDtoIsNull_ShouldThrowNpe() {
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      assertThatThrownBy(() -> attributeTemplatePopulator.update(template, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("UpdateAttributeTemplateDto must not be null");
    }

    @Test
    @DisplayName("should call mapper updateEntityFromDto")
    void update_WhenValid_ShouldCallMapper() {
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      template.setId(UUID.randomUUID());
      UpdateAttributeTemplateRequest request =
          UpdateAttributeTemplateRequest.builder().name("Updated").build();

      attributeTemplatePopulator.update(template, request);

      verify(attributeTemplateMapper).updateEntityFromDto(request, template);
    }
  }

  @Nested
  @DisplayName("toDto(AttributeTemplate template)")
  class ToDtoTests {

    @Test
    @DisplayName("should throw NullPointerException when template is null")
    void toDto_WhenTemplateIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> attributeTemplatePopulator.toDto(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should map template to AttributeTemplateResponse")
    void toDto_WhenTemplateIsValid_ShouldReturnResponse() {
      UUID id = UUID.randomUUID();
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      template.setId(id);

      AttributeTemplateResponse expected =
          AttributeTemplateResponse.builder().id(id).name("T").code("C").build();
      when(attributeTemplateMapper.toDto(template)).thenReturn(expected);

      AttributeTemplateResponse result = attributeTemplatePopulator.toDto(template);

      assertThat(result).isNotNull().isEqualTo(expected);
      verify(attributeTemplateMapper).toDto(template);
    }
  }

  @Nested
  @DisplayName("toListResponse(List<AttributeTemplate> templates)")
  class ToListResponseTests {

    @Test
    @DisplayName("should throw NullPointerException when templates is null")
    void toListResponse_WhenTemplatesIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> attributeTemplatePopulator.toListResponse(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should map templates to ListAttributeTemplateResponse")
    void toListResponse_WhenValid_ShouldReturnResponse() {
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      List<AttributeTemplate> templates = List.of(template);

      UUID id = UUID.randomUUID();
      ShortAttributeTemplateResponse shortDto =
          ShortAttributeTemplateResponse.builder().id(id).name("T").code("C").build();
      when(attributeTemplateMapper.toShortDtoList(templates)).thenReturn(List.of(shortDto));

      ListAttributeTemplateResponse result =
          attributeTemplatePopulator.toListResponse(templates);

      assertThat(result).isNotNull();
      assertThat(result.getTotalCount()).isEqualTo(1);
      assertThat(result.getTemplates()).containsExactly(shortDto);
      verify(attributeTemplateMapper).toShortDtoList(templates);
    }
  }
}
