package vn.io.oldmoon.shopizer.product.infra.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Deprecated
public class SkuFee {
  private UUID id;
  private String name;
  private String description;
  private BigDecimal amount;
  private Currency currency;
  private Boolean required;
}
