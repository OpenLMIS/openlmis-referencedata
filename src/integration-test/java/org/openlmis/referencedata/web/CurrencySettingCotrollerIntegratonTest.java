package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_CODE;
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_SYMBOL;
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_SYMBOL_SIDE;
import static org.openlmis.referencedata.CurrencyConfig.DECIMAL_SEPARATOR;
import static org.openlmis.referencedata.CurrencyConfig.GROUPING_SEPARATOR;
import static org.openlmis.referencedata.CurrencyConfig.GROUPING_SIZE;

import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.openlmis.referencedata.dto.CurrencySettingsDto;

import guru.nidi.ramltester.junit.RamlMatchers;

public class CurrencySettingCotrollerIntegratonTest extends BaseWebIntegrationTest {

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
