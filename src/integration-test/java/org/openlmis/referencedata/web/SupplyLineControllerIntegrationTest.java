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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.referencedata.domain.RightName.SUPPLY_LINES_MANAGE;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.SupplyLineDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.SupplyLineDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.SupplyLineMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyLineControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String PROGRAM = "program";
  private static final String SUPERVISORY_NODE = "supervisoryNode";
  private static final String SUPPLYING_FACILITY = "supplyingFacility";

  private static final String RESOURCE_URL = "/api/supplyLines";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private SupplyLine supplyLine;
  private SupplyLineDto supplyLineDto;
  private UUID supplyLineId;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    supplyLine = generateSupplyLine();
    supplyLineDto = new SupplyLineDto();
    supplyLine.export(supplyLineDto);
    supplyLineId = supplyLine.getId();

    given(supplyLineRepository.save(any(SupplyLine.class))).willAnswer(new SaveAnswer<>());
  }

  @Test
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {

    Map<String, Object> requestBody = getSearchBody();

    restAssured
        .given()
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnBadRequestOnException() {
    Map<String, Object> requestBody = getSearchBody();

    doThrow(new ValidationMessageException(
        SupplyLineMessageKeys.ERROR_SEARCH_LACKS_PARAMS))
        .when(supplyLineService)
        .searchSupplyLines(any(Map.class), any(Pageable.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindSupplyLines() {

    given(supplyLineService.searchSupplyLines(any(Map.class), any(Pageable.class)))
        .willReturn(Pagination.getPage(Arrays.asList(supplyLine), null, 1));

    Map<String, Object> requestBody = getSearchBody();

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteSupplyLine() {
    mockUserHasRight(SUPPLY_LINES_MANAGE);

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

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

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

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

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(null);

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

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

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

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

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

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(null);

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
  public void shouldGetAllSupplyLines() {

    SupplyLine newSupplyLine = generateSupplyLine();
    List<SupplyLine> storedSupplyLines = Arrays.asList(supplyLine, newSupplyLine);
    given(supplyLineRepository.findAll()).willReturn(storedSupplyLines);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body(ID, hasSize(2))
        .body(ID, hasItems(newSupplyLine.getId().toString(), supplyLine.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupplyLine() {

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

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

    given(supplyLineRepository.findOne(supplyLineId)).willReturn(null);

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
    given(supplyLineRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.SUPPLY_LINES_MANAGE);
    given(supplyLineRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.SUPPLY_LINES_MANAGE);
    given(supplyLineRepository.findOne(any(UUID.class))).willReturn(supplyLine);

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

  private Map<String,Object> getSearchBody() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("page", 1);
    requestBody.put("size", 10);
    requestBody.put(PROGRAM, "program-code");
    requestBody.put(SUPERVISORY_NODE, "node-code");
    requestBody.put(SUPPLYING_FACILITY, "facility-code");
    return requestBody;
  }
}
