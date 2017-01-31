package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.referencedata.CurrencyConfig.CURRENCY_CODE;

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
    assertEquals("$", response.getCurrencySymbol());
    assertEquals("left", response.getCurrencySymbolSide());
    assertEquals(CurrencyUnit.of(CURRENCY_CODE).getDecimalPlaces(),
        response.getCurrencyDecimalPlaces());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
