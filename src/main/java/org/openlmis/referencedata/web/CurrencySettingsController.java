/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.web;

import org.joda.money.CurrencyUnit;
import org.openlmis.referencedata.dto.CurrencySettingsDto;
import org.openlmis.referencedata.service.ConfigurationSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_CODE;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_DECIMAL_SEPARATOR;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_GROUPING_SEPARATOR;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_GROUPING_SIZE;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_SYMBOL;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_SYMBOL_SIDE;

@Controller
public class CurrencySettingsController extends BaseController {

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  /**
   * Get currency settings.
   *
   * @return {Version} Returns currency settings from properties.
   */
  @RequestMapping(value = "/currencySettings", method = RequestMethod.GET)
  @ResponseBody
  public CurrencySettingsDto getCurrencySettings() {
    String code = configurationSettingService.getStringValue(CURRENCY_CODE);
    int decimalPlaces = CurrencyUnit.of(code).getDecimalPlaces();
    int groupingSize = Integer.valueOf(configurationSettingService.getStringValue(
            CURRENCY_GROUPING_SIZE));

    return new CurrencySettingsDto(code,
            configurationSettingService.getStringValue(CURRENCY_SYMBOL),
            configurationSettingService.getStringValue(CURRENCY_SYMBOL_SIDE),
            decimalPlaces, configurationSettingService.getStringValue(CURRENCY_GROUPING_SEPARATOR),
            groupingSize, configurationSettingService.getStringValue(CURRENCY_DECIMAL_SEPARATOR));
  }
}
