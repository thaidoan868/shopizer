package vn.io.oldmoon.shopizer.product.business.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.common.core.exception.DuplicateResourceException;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.product.infra.model.product.AttributeTemplate;
import vn.io.oldmoon.shopizer.product.infra.repository.AttributeTemplateRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AttributeTemplateService {

  private final AttributeTemplateRepository attributeTemplateRepository;

  /**
   * Persists a new attribute template.
   *
   * @param template the AttributeTemplate domain entity to persist
   * @return the persisted AttributeTemplate domain entity
   */
  @Transactional
  public AttributeTemplate create(AttributeTemplate template) {
    Objects.requireNonNull(template, "AttributeTemplate must not be null");
    Objects.requireNonNull(template.getCode(), "AttributeTemplate code must not be null");

    if (attributeTemplateRepository.existsByCode(template.getCode())) {
      throw new DuplicateResourceException(
          String.format("Attribute template with code '%s' already exists", template.getCode()));
    }

    log.info("Creating attribute template with code='{}'", template.getCode());
    return attributeTemplateRepository.save(template);
  }

  /**
   * Retrieves all attribute templates.
   *
   * @return list of AttributeTemplate domain entities
   */
  public List<AttributeTemplate> getAll() {
    log.info("Fetching all attribute templates");
    return attributeTemplateRepository.findAll();
  }

  /**
   * Retrieves an attribute template by its UUID.
   *
   * @param id the UUID of the attribute template
   * @return the AttributeTemplate domain entity
   * @throws ResourceNotFoundException if no template is found
   */
  public AttributeTemplate getById(UUID id) {
    Objects.requireNonNull(id, "ID must not be null");
    log.info("Fetching attribute template with id='{}'", id);
    return attributeTemplateRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("AttributeTemplate", id.toString()));
  }

  /**
   * Updates an existing attribute template.
   *
   * @param template the updated AttributeTemplate domain entity
   * @return the saved AttributeTemplate domain entity
   */
  @Transactional
  public AttributeTemplate update(AttributeTemplate template) {
    Objects.requireNonNull(template, "AttributeTemplate must not be null");
    Objects.requireNonNull(template.getCode(), "AttributeTemplate code must not be null");

    if (template.getId() == null) {
      throw new ResourceNotFoundException("AttributeTemplate", "null");
    }

    AttributeTemplate existing =
        attributeTemplateRepository
            .findById(template.getId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "AttributeTemplate", template.getId().toString()));

    boolean codeChanged = !template.getCode().equals(existing.getCode());
    if (codeChanged && attributeTemplateRepository.existsByCode(template.getCode())) {
      throw new DuplicateResourceException(
          String.format("Attribute template with code '%s' already exists", template.getCode()));
    }

    log.info("Updating attribute template with id='{}'", template.getId());
    return attributeTemplateRepository.save(template);
  }

  /**
   * Deletes an attribute template by its UUID.
   *
   * @param id the UUID of the attribute template to delete
   * @throws ResourceNotFoundException if no template is found
   */
  @Transactional
  public void delete(UUID id) {
    AttributeTemplate template = getById(id);
    log.info("Deleting attribute template with id='{}'", id);
    attributeTemplateRepository.delete(template);
  }
}
