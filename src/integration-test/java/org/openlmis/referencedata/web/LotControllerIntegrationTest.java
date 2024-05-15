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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.LOTS_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.LotMessageKeys.ERROR_LOT_CODE_REQUIRED;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.LotDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.LotDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class LotControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/lots";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String ID = "id";

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
    lot.setQuarantined(false);

    lotDto = new LotDto();
    lot.export(lotDto);
    given(lotRepository.save(any(Lot.class))).willAnswer(new SaveAnswer<Lot>());
  }

  @Test
  public void shouldCreateNewLot() {
    mockUserHasRight(LOTS_MANAGE);

    given(lotRepository.search(null, null, lot.getLotCode(), null, null, null, null))
        .willReturn(Pagination.getPage(Collections.emptyList(), PageRequest.of(0, 10)));

    LotDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    assertEquals(lotDto.isQuarantined(), response.isQuarantined());
    assertTrue(lotDto.getExpirationDate().isEqual(response.getExpirationDate()));
    assertTrue(lotDto.getManufactureDate().isEqual(response.getManufactureDate()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateNewLotIfUserHasNoRight() {
    mockUserHasNoRight(LOTS_MANAGE);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    mockUserHasRight(LOTS_MANAGE);

    lotDto.setLotCode("");
    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    mockUserHasRight(LOTS_MANAGE);
    when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));

    given(lotRepository.search(null, null, lot.getLotCode(), null, null, null, null))
        .willReturn(Pagination.getPage(Collections.singletonList(lot), PageRequest.of(0, 10)));

    LotDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    assertEquals(lotDto.isQuarantined(), response.isQuarantined());
    assertTrue(lotDto.getExpirationDate().isEqual(response.getExpirationDate()));
    assertTrue(lotDto.getManufactureDate().isEqual(response.getManufactureDate()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfUpdatedLotDoesNotExist() {
    mockUserHasRight(LOTS_MANAGE);
    when(lotRepository.findById(lotId)).thenReturn(Optional.empty());

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    mockUserHasNoRight(LOTS_MANAGE);
    when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldFindLots() {
    given(lotRepository.search(anyList(), any(LocalDate.class), anyString(),
        nullable(List.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
        .willReturn(Pagination.getPage(singletonList(lot), pageable));
    when(tradeItemRepository.findAllById(anyList()))
        .thenReturn(Collections.singletonList(new TradeItem()));

    String expirationDate = lot.getExpirationDate().format(DateTimeFormatter.ISO_DATE);
    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("tradeItemId", lot.getTradeItem().getId())
        .queryParam("lotCode", lot.getLotCode())
        .queryParam("expirationDate", expirationDate)
        .queryParam("expirationDateFrom", expirationDate)
        .queryParam("expirationDateTo", expirationDate)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnEmptyListWhenTradeItemNotFound() {
    when(tradeItemRepository.findAllById(anyList())).thenReturn(emptyList());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("tradeItemId", lot.getTradeItem().getId())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body("content", hasSize(0));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
            .given()
            .when()
            .get(RESOURCE_URL)
            .then()
            .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetLot() {

    when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));

    LotDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    assertEquals(lot.isQuarantined(), response.isQuarantined());
    assertTrue(lot.getExpirationDate().isEqual(response.getExpirationDate()));
    assertTrue(lot.getManufactureDate().isEqual(response.getManufactureDate()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .pathParam("id", lotId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfLotNotExist() {

    when(lotRepository.findById(lotId)).thenReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", lotId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.LOTS_MANAGE);
    given(lotRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.LOTS_MANAGE);
    given(lotRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.LOTS_MANAGE);
    given(lotRepository.findById(any(UUID.class))).willReturn(Optional.of(lot));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindByUuids() {
    List<Lot> lots = Arrays.asList(
        new LotDataBuilder().build(),
        new LotDataBuilder().build()
    );

    given(lotRepository.search(
        eq(emptyList()),
        eq(null),
        eq(null),
        eq(Arrays.asList(lots.get(0).getId(), lots.get(1).getId())),
        eq(null),
        eq(null),
        any(Pageable.class)
    )).willReturn(Pagination.getPage(lots, PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(ID, lots.get(0).getId())
        .queryParam(ID, lots.get(1).getId())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(2, response.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRespectPaginationParams() {
    List<Lot> lots = Arrays.asList(
        new LotDataBuilder().build(),
        new LotDataBuilder().build()
    );

    given(lotRepository.search(
        eq(emptyList()),
        eq(null),
        eq(null),
        eq(null),
        eq(null),
        eq(null),
        eq(PageRequest.of(0, 2))
    )).willReturn(Pagination.getPage(lots, PageRequest.of(0, 2)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 2)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(2, response.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private TradeItem mockTradeItem() {
    UUID id = UUID.randomUUID();

    TradeItem tradeItem = new TradeItem("name", new ArrayList<>());
    tradeItem.setId(id);
    when(tradeItemRepository.findById(id)).thenReturn(Optional.of(tradeItem));
    return tradeItem;
  }

}
