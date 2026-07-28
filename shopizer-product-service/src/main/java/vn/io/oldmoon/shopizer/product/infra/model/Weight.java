package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import vn.io.oldmoon.shopizer.common.core.constant.WeightUnit;

@Embeddable
@Deprecated
public class Weight {
  @Enumerated(EnumType.STRING)
  private final WeightUnit weightUnit = WeightUnit.g;

  private Double weight;
}
