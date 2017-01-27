package org.openlmis.referencedata.web;

import org.openlmis.referencedata.dto.CurrencySettingsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CurrencySettingsController extends BaseController {

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
  @ResponseBody
  public CurrencySettingsDto getCurrencySettings() {

    return new CurrencySettingsDto(currencyCode, currencySymbol, currencySymbolSide);
  }
}
