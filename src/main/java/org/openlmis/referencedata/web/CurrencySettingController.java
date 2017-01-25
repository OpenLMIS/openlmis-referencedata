package org.openlmis.referencedata.web;

import org.openlmis.referencedata.dto.CurrencySettingDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CurrencySettingController extends BaseController {

  @Value("${currencyCode}")
  private String currencyCode;

  @Value("${currencySymbol}")
  private String currencySymbol;

  @Value("${currencySymbolSide}")
  private String currencySymbolSide;

  /**
   * Get currency settings.
   *
   * @return {Version} Returns currency settings from properties.
   */
  @RequestMapping(value = "/currencySettings", method = RequestMethod.GET)
  public ResponseEntity<CurrencySettingDto> getCurrencySettings() {

    return ResponseEntity
        .ok(new CurrencySettingDto(currencyCode, currencySymbol, currencySymbolSide));
  }
}
