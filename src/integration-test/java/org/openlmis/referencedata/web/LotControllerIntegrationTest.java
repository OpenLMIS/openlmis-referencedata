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

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class LotControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/lots";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private TradeItemRepository tradeItemRepository;

  @MockBean
  private LotRepository lotRepository;

  private Lot lot;
  private UUID lotId;

  @Before
  public void setUp() {
    lot = new Lot();
    lotId = UUID.randomUUID();
    lot.setId(lotId);
    lot.setLotCode("code");
    lot.setTradeItem(mockTradeItem());
    lot.setExpirationDate(ZonedDateTime.now());
    lot.setManufactureDate(ZonedDateTime.now());
    lot.setActive(true);

    given(lotRepository.save(any(Lot.class)))
            .willAnswer(new SaveAnswer<Lot>());
  }

  @Test
  public void shouldCreateNewLot() {
    Lot response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(lot)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(Lot.class);

    assertNotNull(response.getId());
    assertLotsEqual(lot, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateLot() {
    Lot updatedLot = new Lot();
    updatedLot.setId(lotId);
    updatedLot.setLotCode("updatedCode");
    updatedLot.setActive(false);
    updatedLot.setTradeItem(mockTradeItem());
    updatedLot.setExpirationDate(ZonedDateTime.now().minusDays(1));
    updatedLot.setManufactureDate(ZonedDateTime.now().minusDays(1));

    Lot response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", lotId)
        .body(updatedLot)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(Lot.class);

    assertEquals(lotId, response.getId());
    assertLotsEqual(updatedLot, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void assertLotsEqual(Lot expected, Lot actual) {
    assertEquals(expected.getLotCode(), actual.getLotCode());
    assertEquals(expected.isActive(), actual.isActive());
    assertEquals(expected.getTradeItem(), actual.getTradeItem());
    assertTrue(expected.getExpirationDate().isEqual(actual.getExpirationDate()));
    assertTrue(expected.getManufactureDate().isEqual(actual.getManufactureDate()));
  }

  private TradeItem mockTradeItem() {
    UUID id = UUID.randomUUID();
    TradeItem tradeItem = TradeItem.newTradeItem("code " + id.toString(), "unit",
        null, 0, 0, false);
    tradeItem.setId(id);
    when(tradeItemRepository.findOne(id)).thenReturn(tradeItem);
    return tradeItem;
  }

}
