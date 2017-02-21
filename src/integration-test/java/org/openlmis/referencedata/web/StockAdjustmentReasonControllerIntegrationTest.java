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
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.openlmis.referencedata.repository.StockAdjustmentReasonRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.STOCK_ADJUSTMENT_REASONS_MANAGE;

@SuppressWarnings({"PMD.TooManyMethods"})
public class StockAdjustmentReasonControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/stockAdjustmentReasons";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String FIND_BY_PROGRAM_ID_URL = RESOURCE_URL + "/search";

  @MockBean
  private StockAdjustmentReasonRepository stockAdjustmentReasonRepository;

  private StockAdjustmentReason reason;
  private UUID reasonId;
  private Program program;

  /**
   * Constructor for tests.
   */
  public StockAdjustmentReasonControllerIntegrationTest() {
    program = new Program("test");
    program.setName("name");
    program.setDescription("desc");
    program.setId(UUID.randomUUID());
    program.setActive(true);
    program.setPeriodsSkippable(false);
    program.setShowNonFullSupplyTab(false);

    reason = new StockAdjustmentReason(program, "reasonName", "description", true, 1);
    reasonId = UUID.randomUUID();
    reason.setId(reasonId);
  }

  @Test
  public void shouldDeleteStockAdjustmentReason() {
    mockUserHasRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    given(stockAdjustmentReasonRepository.findOne(reasonId)).willReturn(reason);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteStockAdjustmentReasonIfUserHasNoRight() {
    mockUserHasNoRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    given(stockAdjustmentReasonRepository.findOne(reasonId)).willReturn(reason);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostStockAdjustmentReason() {
    mockUserHasRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    when(stockAdjustmentReasonRepository.save(any(StockAdjustmentReason.class))).thenReturn(reason);

    StockAdjustmentReason response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(reason)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(StockAdjustmentReason.class);

    assertNotNull(response.getId());
    assertEquals(reason.getProgram(), response.getProgram());
    assertEquals(reason.getName(), response.getName());
    assertEquals(reason.getDescription(), response.getDescription());
    assertEquals(reason.getAdditive(), response.getAdditive());
    assertEquals(reason.getDisplayOrder(), response.getDisplayOrder());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostStockAdjustmentReasonIfUserHasNoRight() {
    mockUserHasNoRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(reason)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutStockAdjustmentReason() {
    mockUserHasRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    given(stockAdjustmentReasonRepository.findOne(reasonId)).willReturn(reason);

    StockAdjustmentReason response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonId)
        .body(reason)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason.class);

    assertEquals(reasonId, response.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutStockAdjustmentReasonIfUserHasNoRight() {
    mockUserHasNoRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    given(stockAdjustmentReasonRepository.findOne(reasonId)).willReturn(reason);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonId)
        .body(reason)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllStockAdjustmentReasons() {
    mockUserHasNoRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    StockAdjustmentReason anotherReason = new StockAdjustmentReason(
        program, "reason2", "description", true, 1);
    anotherReason.setId(UUID.randomUUID());

    List<StockAdjustmentReason> storedStockAdjustmentReasons =
        Arrays.asList(reason, anotherReason);
    given(stockAdjustmentReasonRepository.findAll()).willReturn(storedStockAdjustmentReasons);

    StockAdjustmentReason[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason[].class);

    assertEquals(storedStockAdjustmentReasons.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetStockAdjustmentReason() {
    mockUserHasNoRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    given(stockAdjustmentReasonRepository.findOne(reasonId)).willReturn(reason);

    StockAdjustmentReason response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", reasonId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason.class);

    assertEquals(reasonId, response.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindStockAdjustmentReasonsByProgramId() {
    mockUserHasNoRight(STOCK_ADJUSTMENT_REASONS_MANAGE);

    UUID programId = UUID.randomUUID();
    List<StockAdjustmentReason> listToReturn = new ArrayList<>();
    listToReturn.add(reason);
    given(stockAdjustmentReasonRepository.findByProgramId(programId))
        .willReturn(listToReturn);

    StockAdjustmentReason[] response = restAssured.given()
        .queryParam("program", programId)
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(FIND_BY_PROGRAM_ID_URL)
        .then()
        .statusCode(200)
        .extract().as(StockAdjustmentReason[].class);

    List<StockAdjustmentReason> foundStockAdjustmentReason = Arrays.asList(response);
    assertEquals(1, foundStockAdjustmentReason.size());
    assertEquals(reasonId, foundStockAdjustmentReason.get(0).getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
