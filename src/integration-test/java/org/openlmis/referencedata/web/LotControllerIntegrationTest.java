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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.LotMessageKeys.ERROR_LOT_CODE_REQUIRED;

import com.fasterxml.jackson.core.JsonProcessingException;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.LotDto;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class LotControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/lots";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  private Lot lot;
  private UUID lotId;
  LotDto lotDto;

  @Before
  public void setUp() {
    lot = new Lot();
    lotId = UUID.randomUUID();
    lot.setId(lotId);
    lot.setLotCode("code");
    lot.setTradeItem(mockTradeItem());
    lot.setExpirationDate(LocalDate.now());
    lot.setManufactureDate(LocalDate.now());
    lot.setActive(true);

    lotDto = new LotDto();
    lot.export(lotDto);
    given(lotRepository.save(any(Lot.class))).willAnswer(new SaveAnswer<Lot>());
  }

  @Test
  public void shouldCreateNewLot() {
    mockUserHasRight(ORDERABLES_MANAGE);

    LotDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(lotDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(LotDto.class);

    assertNotNull(response.getId());
    assertEquals(lotDto.getLotCode(), response.getLotCode());
    assertEquals(lotDto.isActive(), response.isActive());
    assertEquals(lotDto.getTradeItemId(), response.getTradeItemId());
    assertTrue(lotDto.getExpirationDate().isEqual(response.getExpirationDate()));
    assertTrue(lotDto.getManufactureDate().isEqual(response.getManufactureDate()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateNewLotIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
            .given()
            .queryParam(ACCESS_TOKEN, getToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(lotDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateNewLotIfLotCodeIsEmpty() {
    mockUserHasRight(ORDERABLES_MANAGE);

    lotDto.setLotCode("");
    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(lotDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_LOT_CODE_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateLot() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(lotRepository.findOne(lotId)).thenReturn(lot);

    LotDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", lotId)
        .body(lotDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(LotDto.class);

    assertEquals(lotDto.getId(), response.getId());
    assertEquals(lotDto.getLotCode(), response.getLotCode());
    assertEquals(lotDto.isActive(), response.isActive());
    assertEquals(lotDto.getTradeItemId(), response.getTradeItemId());
    assertTrue(lotDto.getExpirationDate().isEqual(response.getExpirationDate()));
    assertTrue(lotDto.getManufactureDate().isEqual(response.getManufactureDate()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfUpdatedLotDoesNotExist() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(lotRepository.findOne(lotId)).thenReturn(null);

    restAssured
            .given()
            .queryParam(ACCESS_TOKEN, getToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", lotId)
            .body(lotDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectUpdateLotIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);
    when(lotRepository.findOne(lotId)).thenReturn(lot);

    restAssured
            .given()
            .queryParam(ACCESS_TOKEN, getToken())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", lotId)
            .body(lotDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindLots() throws JsonProcessingException {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(lotRepository.search(any(TradeItem.class), any(LocalDate.class), anyString()))
        .willReturn(singletonList(lot));

    PageImplRepresentation response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("tradeIdemId", lot.getTradeItem().getId())
        .queryParam("lotCode", lot.getLotCode())
        .queryParam("expirationDate",
            lot.getExpirationDate().format(DateTimeFormatter.ISO_DATE))
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenTradeItemNotFound() throws JsonProcessingException {
    mockUserHasRight(ORDERABLES_MANAGE);

    when(tradeItemRepository.findOne(any(UUID.class))).thenReturn(null);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("tradeIdemId", lot.getTradeItem().getId())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectSearchLotsIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
            .given()
            .queryParam(ACCESS_TOKEN, getToken())
            .when()
            .get(SEARCH_URL)
            .then()
            .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetLot() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(lotRepository.findOne(lotId)).thenReturn(lot);

    LotDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", lotId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(LotDto.class);

    assertEquals(lot.getId(), response.getId());
    assertEquals(lot.getLotCode(), response.getLotCode());
    assertEquals(lot.isActive(), response.isActive());
    assertEquals(lot.getTradeItem().getId(), response.getTradeItemId());
    assertTrue(lot.getExpirationDate().isEqual(response.getExpirationDate()));
    assertTrue(lot.getManufactureDate().isEqual(response.getManufactureDate()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetLotIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", lotId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfLotNotExist() {
    mockUserHasRight(ORDERABLES_MANAGE);
    when(lotRepository.findOne(lotId)).thenReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", lotId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private TradeItem mockTradeItem() {
    UUID id = UUID.randomUUID();

    TradeItem tradeItem = new TradeItem("name", new ArrayList<>());
    tradeItem.setId(id);
    when(tradeItemRepository.findOne(id)).thenReturn(tradeItem);
    return tradeItem;
  }

}
