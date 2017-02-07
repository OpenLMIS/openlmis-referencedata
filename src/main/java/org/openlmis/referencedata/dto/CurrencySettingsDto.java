package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CurrencySettingsDto {

  private String currencyCode;

  private String currencySymbol;

  private String currencySymbolSide;

  private int currencyDecimalPlaces;

  private String groupingSeparator;

  private int groupingSize;

  private String decimalSeparator;
}
