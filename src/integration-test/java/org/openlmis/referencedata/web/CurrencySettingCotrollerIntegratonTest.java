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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_CODE;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_DECIMAL_SEPARATOR;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_GROUPING_SEPARATOR;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_GROUPING_SIZE;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_SYMBOL;
import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_SYMBOL_SIDE;

import org.joda.money.CurrencyUnit;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.ConfigurationSetting;
import org.openlmis.referencedata.dto.CurrencySettingsDto;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.openlmis.referencedata.repository.ConfigurationSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CurrencySettingCotrollerIntegratonTest extends BaseWebIntegrationTest {

  private static final String EXPECTED_CURRENCY_CODE = "USD";
  private static final String EXPECTED_CURRENCY_SYMBOL = "$";
  private static final String EXPECTED_CURRENCY_SYMBOL_SIDE = "left";
  private static final String EXPECTED_GROUPING_SEPARATOR = ",";
  private static final String EXPECTED_GROUPING_SIZE = "3";
  private static final String EXPECTED_DECIMAL_SEPARATOR = ".";
  
  @Autowired
  private ConfigurationSettingRepository configurationSettingRepository;
  
  @Before
  public void setUp() {
    configurationSettingRepository.save(
            new ConfigurationSetting(CURRENCY_CODE, EXPECTED_CURRENCY_CODE));
    configurationSettingRepository.save(
            new ConfigurationSetting(CURRENCY_SYMBOL, EXPECTED_CURRENCY_SYMBOL));
    configurationSettingRepository.save(
            new ConfigurationSetting(CURRENCY_SYMBOL_SIDE, EXPECTED_CURRENCY_SYMBOL_SIDE));
    configurationSettingRepository.save(
            new ConfigurationSetting(CURRENCY_GROUPING_SEPARATOR, EXPECTED_GROUPING_SEPARATOR));
    configurationSettingRepository.save(
            new ConfigurationSetting(CURRENCY_GROUPING_SIZE, EXPECTED_GROUPING_SIZE));
    configurationSettingRepository.save(
            new ConfigurationSetting(CURRENCY_DECIMAL_SEPARATOR, EXPECTED_DECIMAL_SEPARATOR));
    
  }
  
  @Test
  public void shouldReturnCurrencySetting() {

    CurrencySettingsDto response = new CurrencySettingsDto();
    response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get("api/currencySettings")
        .then()
        .statusCode(200)
        .extract().as(response.getClass());

    assertEquals(EXPECTED_CURRENCY_CODE, response.getCurrencyCode());
    assertEquals(EXPECTED_CURRENCY_SYMBOL, response.getCurrencySymbol());
    assertEquals(EXPECTED_CURRENCY_SYMBOL_SIDE, response.getCurrencySymbolSide());
    assertEquals(CurrencyUnit.of(EXPECTED_CURRENCY_CODE).getDecimalPlaces(),
        response.getCurrencyDecimalPlaces());
    assertEquals(EXPECTED_GROUPING_SEPARATOR, response.getGroupingSeparator());
    assertEquals(EXPECTED_GROUPING_SIZE, String.valueOf(response.getGroupingSize()));
    assertEquals(EXPECTED_DECIMAL_SEPARATOR, response.getDecimalSeparator());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
