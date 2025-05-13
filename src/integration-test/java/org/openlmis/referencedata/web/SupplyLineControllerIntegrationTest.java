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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.referencedata.domain.RightName.SUPPLY_LINES_MANAGE;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.SupplyLineDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.SupplyLineDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyLineControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String PROGRAM_ID = "programId";
  private static final String SUPERVISORY_NODE_ID = "supervisoryNodeId";
  private static final String SUPPLYING_FACILITY_ID = "supplyingFacilityId";

  private static final String RESOURCE_URL = "/api/supplyLines";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private SupplyLine supplyLine;
  private SupplyLineDto supplyLineDto;
  private UUID supplyLineId;
  private Pageable pageable;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    supplyLine = generateSupplyLine();
    supplyLineDto = new SupplyLineDto();
    supplyLine.export(supplyLineDto);
    supplyLineId = supplyLine.getId();
    pageable = PageRequest.of(0, 10);

    given(supplyLineRepository.save(any(SupplyLine.class))).willAnswer(new SaveAnswer<>());
  }

  @Test
  public void searchShouldReturnBadRequestOnException() {
    Map<String, Object> requestParams = getSearchParameters();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .params(requestParams)
        .param("some-unknown-parameter", "some-value")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindSupplyLinesWithoutParameters() {
    Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);

    given(supplyLineRepository.search(null, null, emptySet(), pageable))
        .willReturn(Pagination.getPage(singletonList(supplyLine), pageable, 1));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.getContent().size());
  }

  @Test
  public void shouldFindSupplyLines() {
    given(supplyLineRepository.search(
        supplyLine.getProgram().getId(),
        supplyLine.getSupervisoryNode().getId(),
        singleton(supplyLine.getSupplyingFacility().getId()),
        pageable))
        .willReturn(Pagination.getPage(singletonList(supplyLine), pageable, 1));

    Map<String, Object> requestBody = getSearchParameters();

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .params(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.getContent().size());
  }

  @Test
  public void shouldFindSupplyLinesWithExpand() {
    given(supplyLineRepository.search(
        supplyLine.getProgram().getId(),
        supplyLine.getSupervisoryNode().getId(),
        singleton(supplyLine.getSupplyingFacility().getId()),
        pageable))
        .willReturn(Pagination.getPage(singletonList(supplyLine), pageable, 1));

    Map<String, Object> requestBody = getSearchParameters();
    requestBody.put("expand", "supervisoryNode");

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .params(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.getContent().size());
  }

  @Test
  public void shouldDeleteSupplyLine() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.of(supplyLine));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteSupplyLineIfUserHasNoRight() {
    mockUserHasNoRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.of(supplyLine));

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
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
  public void shouldNotDeleteNonexistentSupplyLine() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostSupplyLine() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supplyLineDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .body(ID, is(notNullValue(String.class)));

    assertResponseBody(response);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostSupplyLineIfUserHasNoRight() {
    mockUserHasNoRight(SUPPLY_LINES_MANAGE);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supplyLineDto)
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
  public void shouldPutSupplyLine() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.of(supplyLine));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .body(supplyLineDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .body(ID, is(supplyLineId.toString()));

    assertResponseBody(response);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupplyLineIfUserHasNoRight() {
    mockUserHasNoRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.of(supplyLine));

    supplyLine.export(supplyLineDto);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .body(supplyLineDto)
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
  public void shouldCreateNewSupplyLineIfDoesNotExist() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.empty());

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .body(supplyLineDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .body(ID, is(notNullValue(String.class)));

    assertResponseBody(response);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupplyLine() {

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.of(supplyLine));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .body(ID, is(supplyLineId.toString()));

    assertResponseBody(response);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentSupplyLine() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findById(supplyLineId)).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
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
        .checkAdminRight(RightName.SUPPLY_LINES_MANAGE);
    given(supplyLineRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.SUPPLY_LINES_MANAGE);
    given(supplyLineRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.SUPPLY_LINES_MANAGE);
    given(supplyLineRepository.findById(any(UUID.class))).willReturn(Optional.of(supplyLine));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void assertResponseBody(ValidatableResponse response) {
    response
        .body("supervisoryNode.id", is(supplyLine.getSupervisoryNode().getId().toString()))
        .body("description", is(supplyLine.getDescription()))
        .body("program.id", is(supplyLine.getProgram().getId().toString()))
        .body("supplyingFacility.id", is(supplyLine.getSupplyingFacility().getId().toString()));
  }

  private SupplyLine generateSupplyLine() {
    return new SupplyLineDataBuilder().build();
  }

  private Map<String,Object> getSearchParameters() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("page", 0);
    parameters.put("size", 10);
    parameters.put(PROGRAM_ID, supplyLine.getProgram().getId());
    parameters.put(SUPERVISORY_NODE_ID, supplyLine.getSupervisoryNode().getId());
    parameters.put(SUPPLYING_FACILITY_ID, supplyLine.getSupplyingFacility().getId());
    return parameters;
  }
}
