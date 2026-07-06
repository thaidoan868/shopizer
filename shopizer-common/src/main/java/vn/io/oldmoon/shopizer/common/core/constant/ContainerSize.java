package vn.io.oldmoon.shopizer.common.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Deprecated
public enum ContainerSize {
  S(20, 20, 20, DimensionUnit.cm),
  M(40, 40, 40, DimensionUnit.cm),
  XL(100, 100, 100, DimensionUnit.cm),
  XLS(120, 80, 80, DimensionUnit.cm),
  XLSX(120, 100, 120, DimensionUnit.cm);

  private final double width;
  private final double height;
  private final double depth;
  private final DimensionUnit unitOfMeasure;
}
