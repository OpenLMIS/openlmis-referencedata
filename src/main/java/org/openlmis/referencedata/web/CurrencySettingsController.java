package org.openlmis.referencedata.web;

import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.dto.CurrencySettingsDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CurrencySettingsController extends BaseController {

  /**
   * Get currency settings.
   *
   * @return {Version} Returns currency settings from properties.
   */
  @RequestMapping(value = "/currencySettings", method = RequestMethod.GET)
  @ResponseBody
  public CurrencySettingsDto getCurrencySettings() {

    return new CurrencySettingsDto(CurrencyConfig.CURRENCY_CODE, CurrencyConfig.CURRENCY_SYMBOL,
        CurrencyConfig.CURRENCY_SYMBOL_SIDE, CurrencyConfig.CURRENCY_DECIMAL_PLACES,
        CurrencyConfig.GROUPING_SEPARATOR, CurrencyConfig.GROUPING_SIZE,
        CurrencyConfig.DECIMAL_SEPARATOR);
  }
}
