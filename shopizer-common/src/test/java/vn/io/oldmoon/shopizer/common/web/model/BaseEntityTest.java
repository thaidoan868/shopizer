package vn.io.oldmoon.shopizer.common.web.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BaseEntityTest {
  // Concrete mock subclass just for testing purposes
  private static class TestEntity extends BaseEntity {}

  @Nested
  class EqualsTests {
    @Test
    void equals_ShouldReturnTrue_WhenSameInstance() {
      TestEntity entity = new TestEntity();

      assertThat(entity.equals(entity)).isTrue();
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparedWithNullOrDifferentClass() {
      TestEntity entity = new TestEntity();

      assertThat(entity.equals(null)).isFalse();
      assertThat(entity.equals("not an entity")).isFalse();
    }

    @Test
    void equals_ShouldReturnTrue_WhenBothHaveSameNonNullId() {
      UUID sharedId = UUID.randomUUID();
      TestEntity entity1 = new TestEntity();
      TestEntity entity2 = new TestEntity();

      entity1.setId(sharedId);
      entity2.setId(sharedId);

      assertThat(entity1.equals(entity2)).isTrue();
      assertThat(entity2.equals(entity1)).isTrue();
    }

    @Test
    void equals_ShouldReturnFalse_WhenIdsAreDifferent() {
      TestEntity entity1 = new TestEntity();
      TestEntity entity2 = new TestEntity();

      entity1.setId(UUID.randomUUID());
      entity2.setId(UUID.randomUUID());

      assertThat(entity1.equals(entity2)).isFalse();
    }

    @Test
    void equals_ShouldReturnFalse_WhenOneOrBothIdsAreNull() {
      TestEntity entity1 = new TestEntity();
      TestEntity entity2 = new TestEntity();

      entity1.setId(UUID.randomUUID());
      // entity2 has null ID

      assertThat(entity1.equals(entity2)).isFalse();

      // Both null IDs
      entity1.setId(null);
      assertThat(entity1.equals(entity2)).isFalse();
    }
  }

  @Nested
  class HashCodeTests {

    @Test
    void hashCode_ShouldReturnIdHashCode_WhenIdIsNotNull() {
      UUID id = UUID.randomUUID();
      TestEntity entity = new TestEntity();
      entity.setId(id);

      assertThat(entity.hashCode()).isEqualTo(id.hashCode());
    }

    @Test
    void hashCode_ShouldReturnIdentityHashCode_WhenIdIsNull() {
      TestEntity entity = new TestEntity();

      assertThat(entity.hashCode()).isEqualTo(System.identityHashCode(entity));
    }

    @Test
    void hashCode_ShouldReturnDifferentHashCode_WhenIdIsNull() {
      TestEntity entity1 = new TestEntity();
      TestEntity entity2 = new TestEntity();
      assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
    }
  }
}
