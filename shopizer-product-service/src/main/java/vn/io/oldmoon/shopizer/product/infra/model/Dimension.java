package vn.io.oldmoon.shopizer.product.infra.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import vn.io.oldmoon.shopizer.common.core.constant.ContainerSize;
import vn.io.oldmoon.shopizer.common.core.constant.DimensionUnit;

@Embeddable
@Data
@Deprecated
public class Dimension {
  @Enumerated(EnumType.STRING)
  private DimensionUnit dimensionUnit = DimensionUnit.cm;

  private Double width;
  private Double height;
  private Double depth;

  private Double containerWidth = ContainerSize.M.getWidth();
  private Double containerHeight = ContainerSize.M.getHeight();
  private Double containerDepth = ContainerSize.M.getDepth();
}
