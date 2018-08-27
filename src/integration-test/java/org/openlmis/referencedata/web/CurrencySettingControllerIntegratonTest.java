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
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_CODE;
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_SYMBOL;
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_SYMBOL_SIDE;
import static org.openlmis.referencedata.CurrencyConfig.DECIMAL_SEPARATOR;
import static org.openlmis.referencedata.CurrencyConfig.GROUPING_SEPARATOR;
import static org.openlmis.referencedata.CurrencyConfig.GROUPING_SIZE;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.openlmis.referencedata.dto.CurrencySettingsDto;
import org.springframework.http.HttpHeaders;

public class CurrencySettingControllerIntegratonTest extends BaseWebIntegrationTest {

  @Test
  public void shouldReturnCurrencySetting() {

    CurrencySettingsDto response = new CurrencySettingsDto();
    response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get("api/currencySettings")
        .then()
        .statusCode(200)
        .extract().as(response.getClass());

    assertEquals(CURRENCY_CODE, response.getCurrencyCode());
    assertEquals(CURRENCY_SYMBOL, response.getCurrencySymbol());
    assertEquals(CURRENCY_SYMBOL_SIDE, response.getCurrencySymbolSide());
    assertEquals(CurrencyUnit.of(CURRENCY_CODE).getDecimalPlaces(),
        response.getCurrencyDecimalPlaces());
    assertEquals(GROUPING_SEPARATOR, response.getGroupingSeparator());
    assertEquals(GROUPING_SIZE, response.getGroupingSize());
    assertEquals(DECIMAL_SEPARATOR, response.getDecimalSeparator());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
