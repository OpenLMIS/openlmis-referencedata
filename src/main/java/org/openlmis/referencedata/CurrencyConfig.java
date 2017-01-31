package org.openlmis.referencedata;

import org.joda.money.CurrencyUnit;

public final class CurrencyConfig {

  public static final String CURRENCY_CODE = "USD";
  public static final String CURRENCY_SYMBOL = "$";
  public static final String CURRENCY_SYMBOL_SIDE = "left";
  public static final int CURRENCY_DECIMAL_PLACES =
      CurrencyUnit.of(CURRENCY_CODE).getDecimalPlaces();

  private CurrencyConfig() {
  }
}
