package vn.io.oldmoon.shopizer.product.infra.model;

import java.math.BigDecimal;

@Deprecated
public enum Tax {
  // VAT
  VAT("0.1", "Value added tax", "Standard rate for most goods"),

  // Excise Tax
  EXCISE_TOBACCO(
      "0.75", "Excise Tax - Tobacco", "Cigarettes, cigars and other tobacco preparations"),
  EXCISE_BEER("0.65", "Excise Tax - Beer", "All types of beer"),
  EXCISE_LIQUOR_HIGH("0.65", "Excise Tax - Strong Liquor", "Spirits with 20% ABV or higher"),
  EXCISE_LIQUOR_LOW("0.35", "Excise Tax - Light Liquor", "Spirits with less than 20% ABV"),
  EXCISE_CAR_SMALL(
      "0.35", "Excise Tax - Small Car", "Passenger cars under 9 seats, cylinder capacity < 1.5L"),
  EXCISE_CAR_LUXURY(
      "1.5", "Excise Tax - Luxury Car", "Passenger cars with cylinder capacity > 6.0L"),
  EXCISE_ELECTRIC_CAR(
      "0.03", "Excise Tax - EV", "Battery electric cars under 9 seats (incentive rate)"),
  EXCISE_GASOLINE("0.1", "Excise Tax - Gasoline", "Various types of gasoline");

  private final BigDecimal taxRate;
  private final String name;
  private final String description;

  Tax(String taxRate, String name, String description) {
    this.taxRate = new BigDecimal(taxRate);
    this.name = name;
    this.description = description;
  }
}
