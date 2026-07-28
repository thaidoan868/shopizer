package vn.io.oldmoon.shopizer.common.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
  @Id @UuidGenerator private UUID id;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant created;

  @CreatedBy
  @Column(updatable = false)
  private UUID createdBy;

  @LastModifiedBy private Instant modified;
  @UpdateTimestamp private UUID modifiedBy;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseEntity object)) {
      return false;
    }
    if (this.id != null && object.id != null) {
      return this.id.equals(object.id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : System.identityHashCode(this);
  }
}
