package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.referencedata.dto.CurrencySettingDto;

import guru.nidi.ramltester.junit.RamlMatchers;

public class CurrencySettingCotrollerIntegratonTest extends BaseWebIntegrationTest {

  @Test
  public void shouldReturnCurrencySetting() {

    CurrencySettingDto response = new CurrencySettingDto();
    response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get("api/currencySettings")
        .then()
        .statusCode(200)
        .extract().as(response.getClass());

    assertEquals("USD", response.getCurrencyCode());
    assertEquals("$", response.getCurrencySymbol());
    assertEquals("left", response.getCurrencySymbolSide());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
