package vn.io.oldmoon.shopizer.product.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.oldmoon.shopizer.common.core.exception.DuplicateResourceException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;
import vn.io.oldmoon.shopizer.product.infra.repository.AttributeTemplateRepository;

@ExtendWith(MockitoExtension.class)
class AttributeTemplateServiceTest {

  @Mock private AttributeTemplateRepository attributeTemplateRepository;

  @InjectMocks private AttributeTemplateService attributeTemplateService;

  @Nested
  @DisplayName("create(AttributeTemplate template)")
  class CreateTests {

    @Test
    @DisplayName("should throw NullPointerException when template is null")
    void create_WhenTemplateIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> attributeTemplateService.create(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("AttributeTemplate must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when template code is null")
    void create_WhenCodeIsNull_ShouldThrowNpe() {
      AttributeTemplate template = AttributeTemplate.builder().name("Template").build();
      assertThatThrownBy(() -> attributeTemplateService.create(template))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("AttributeTemplate code must not be null");
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when code already exists")
    void create_WhenCodeExists_ShouldThrowDuplicateResourceException() {
      AttributeTemplate template =
          AttributeTemplate.builder().name("Template").code("GPU_CODE").build();
      when(attributeTemplateRepository.existsByCode("GPU_CODE")).thenReturn(true);

      assertThatThrownBy(() -> attributeTemplateService.create(template))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Attribute template with code 'GPU_CODE' already exists");
      verify(attributeTemplateRepository).existsByCode("GPU_CODE");
    }

    @Test
    @DisplayName("should save and return template when code is unique")
    void create_WhenCodeIsUnique_ShouldSaveAndReturn() {
      AttributeTemplate template =
          AttributeTemplate.builder().name("Template").code("GPU_CODE").build();
      when(attributeTemplateRepository.existsByCode("GPU_CODE")).thenReturn(false);
      when(attributeTemplateRepository.save(template)).thenReturn(template);

      AttributeTemplate result = attributeTemplateService.create(template);

      assertThat(result).isNotNull().isEqualTo(template);
      verify(attributeTemplateRepository).existsByCode("GPU_CODE");
      verify(attributeTemplateRepository).save(template);
    }
  }

  @Nested
  @DisplayName("getAll()")
  class GetAllTests {

    @Test
    @DisplayName("should return all attribute templates")
    void getAll_ShouldReturnList() {
      AttributeTemplate t1 = AttributeTemplate.builder().name("T1").code("C1").build();
      AttributeTemplate t2 = AttributeTemplate.builder().name("T2").code("C2").build();
      List<AttributeTemplate> expected = List.of(t1, t2);
      when(attributeTemplateRepository.findAll()).thenReturn(expected);

      List<AttributeTemplate> result = attributeTemplateService.getAll();

      assertThat(result).isNotNull().hasSize(2).isEqualTo(expected);
      verify(attributeTemplateRepository).findAll();
    }
  }

  @Nested
  @DisplayName("getById(UUID id)")
  class GetByIdTests {

    @Test
    @DisplayName("should throw NullPointerException when id is null")
    void getById_WhenIdIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> attributeTemplateService.getById(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("ID must not be null");
    }

    @Test
    @DisplayName("should return template when found by id")
    void getById_WhenFound_ShouldReturnTemplate() {
      UUID id = UUID.randomUUID();
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      template.setId(id);
      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.of(template));

      AttributeTemplate result = attributeTemplateService.getById(id);

      assertThat(result).isNotNull().isEqualTo(template);
      verify(attributeTemplateRepository).findById(id);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when not found by id")
    void getById_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> attributeTemplateService.getById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("AttributeTemplate")
          .hasMessageContaining(id.toString());
      verify(attributeTemplateRepository).findById(id);
    }
  }

  @Nested
  @DisplayName("update(AttributeTemplate template)")
  class UpdateTests {

    @Test
    @DisplayName("should throw NullPointerException when template is null")
    void update_WhenTemplateIsNull_ShouldThrowNpe() {
      assertThatThrownBy(() -> attributeTemplateService.update(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("AttributeTemplate must not be null");
    }

    @Test
    @DisplayName("should throw NullPointerException when code is null")
    void update_WhenCodeIsNull_ShouldThrowNpe() {
      AttributeTemplate template = AttributeTemplate.builder().name("T").build();
      assertThatThrownBy(() -> attributeTemplateService.update(template))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("AttributeTemplate code must not be null");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when template id is null")
    void update_WhenIdIsNull_ShouldThrowResourceNotFoundException() {
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      assertThatThrownBy(() -> attributeTemplateService.update(template))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("AttributeTemplate")
          .hasMessageContaining("null");
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when template does not exist")
    void update_WhenTemplateDoesNotExist_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      AttributeTemplate template = AttributeTemplate.builder().name("T").code("C").build();
      template.setId(id);
      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> attributeTemplateService.update(template))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("AttributeTemplate")
          .hasMessageContaining(id.toString());
      verify(attributeTemplateRepository).findById(id);
    }

    @Test
    @DisplayName("should save and return template when code is unchanged")
    void update_WhenCodeIsUnchanged_ShouldSaveAndReturn() {
      UUID id = UUID.randomUUID();
      AttributeTemplate existing = AttributeTemplate.builder().name("Old Name").code("SAME_CODE").build();
      existing.setId(id);

      AttributeTemplate updateTemplate =
          AttributeTemplate.builder().name("New Name").code("SAME_CODE").build();
      updateTemplate.setId(id);

      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.of(existing));
      when(attributeTemplateRepository.save(updateTemplate)).thenReturn(updateTemplate);

      AttributeTemplate result = attributeTemplateService.update(updateTemplate);

      assertThat(result).isNotNull().isEqualTo(updateTemplate);
      verify(attributeTemplateRepository).findById(id);
      verify(attributeTemplateRepository).save(updateTemplate);
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when code is changed and already exists")
    void update_WhenCodeChangedAndAlreadyExists_ShouldThrowDuplicateResourceException() {
      UUID id = UUID.randomUUID();
      AttributeTemplate existing = AttributeTemplate.builder().name("Old Name").code("OLD_CODE").build();
      existing.setId(id);

      AttributeTemplate updateTemplate =
          AttributeTemplate.builder().name("New Name").code("NEW_CODE").build();
      updateTemplate.setId(id);

      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.of(existing));
      when(attributeTemplateRepository.existsByCode("NEW_CODE")).thenReturn(true);

      assertThatThrownBy(() -> attributeTemplateService.update(updateTemplate))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Attribute template with code 'NEW_CODE' already exists");
      verify(attributeTemplateRepository).findById(id);
      verify(attributeTemplateRepository).existsByCode("NEW_CODE");
    }

    @Test
    @DisplayName("should save and return template when code is changed and does not exist")
    void update_WhenCodeChangedAndDoesNotExist_ShouldSaveAndReturn() {
      UUID id = UUID.randomUUID();
      AttributeTemplate existing = AttributeTemplate.builder().name("Old Name").code("OLD_CODE").build();
      existing.setId(id);

      AttributeTemplate updateTemplate =
          AttributeTemplate.builder().name("New Name").code("NEW_CODE").build();
      updateTemplate.setId(id);

      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.of(existing));
      when(attributeTemplateRepository.existsByCode("NEW_CODE")).thenReturn(false);
      when(attributeTemplateRepository.save(updateTemplate)).thenReturn(updateTemplate);

      AttributeTemplate result = attributeTemplateService.update(updateTemplate);

      assertThat(result).isNotNull().isEqualTo(updateTemplate);
      verify(attributeTemplateRepository).findById(id);
      verify(attributeTemplateRepository).existsByCode("NEW_CODE");
      verify(attributeTemplateRepository).save(updateTemplate);
    }
  }

  @Nested
  @DisplayName("delete(UUID id)")
  class DeleteTests {

    @Test
    @DisplayName("should delete template when found by id")
    void delete_WhenFound_ShouldDelete() {
      UUID id = UUID.randomUUID();
      AttributeTemplate existing = AttributeTemplate.builder().name("T").code("C").build();
      existing.setId(id);

      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.of(existing));

      attributeTemplateService.delete(id);

      verify(attributeTemplateRepository).findById(id);
      verify(attributeTemplateRepository).delete(existing);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when id not found")
    void delete_WhenNotFound_ShouldThrowResourceNotFoundException() {
      UUID id = UUID.randomUUID();
      when(attributeTemplateRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> attributeTemplateService.delete(id))
          .isInstanceOf(ResourceNotFoundException.class);
      verify(attributeTemplateRepository).findById(id);
    }
  }
}
