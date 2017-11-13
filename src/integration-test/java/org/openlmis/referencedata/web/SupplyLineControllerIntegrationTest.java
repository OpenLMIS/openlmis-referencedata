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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.referencedata.domain.RightName.SUPPLY_LINES_MANAGE;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.SupplyLineDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.SupplyLineMessageKeys;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import org.openlmis.referencedata.utils.AuditLogHelper;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class SupplyLineControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String PROGRAM = "program";
  private static final String SUPERVISORY_NODE = "supervisoryNode";
  private static final String SUPPLYING_FACILITY = "supplyingFacility";

  private static final String RESOURCE_URL = "/api/supplyLines";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DESCRIPTION = "OpenLMIS";

  private SupplyLine supplyLine;
  private SupplyLineDto supplyLineDto;
  private UUID supplyLineId;
  private Integer currentInstanceNumber;

  /**
   * Constructor for tests.
   */
  public SupplyLineControllerIntegrationTest() {
    currentInstanceNumber = 0;
    supplyLine = generateSupplyLine();
    supplyLineDto = new SupplyLineDto();
    supplyLine.export(supplyLineDto);
    supplyLineId = UUID.randomUUID();
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

    SupplyLineDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supplyLineDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(SupplyLineDto.class);

    assertEquals(supplyLineDto, response);
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

    supplyLine.setDescription(DESCRIPTION);
    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

    SupplyLineDto supplyLineDto = new SupplyLineDto();
    supplyLine.export(supplyLineDto);

    SupplyLineDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .body(supplyLineDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(SupplyLineDto.class);

    assertEquals(supplyLineDto, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutSupplyLineIfUserHasNoRight() {
    mockUserHasNoRight(SUPPLY_LINES_MANAGE);

    supplyLine.setDescription(DESCRIPTION);
    given(supplyLineRepository.findOne(supplyLineId)).willReturn(supplyLine);

    SupplyLineDto supplyLineDto = new SupplyLineDto();
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

    supplyLine.setDescription(DESCRIPTION);
    given(supplyLineRepository.findOne(supplyLineId)).willReturn(null);

    SupplyLineDto supplyLineDto = new SupplyLineDto();
    supplyLine.export(supplyLineDto);

    SupplyLineDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .body(supplyLineDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(SupplyLineDto.class);

    assertEquals(supplyLineDto, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSupplyLines() {

    SupplyLine newSupplyLine = generateSupplyLine();
    List<SupplyLine> storedSupplyLines = Arrays.asList(supplyLine, newSupplyLine);
    given(supplyLineRepository.findAll()).willReturn(storedSupplyLines);

    SupplyLineDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(SupplyLineDto[].class);

    assertEquals(storedSupplyLines.size(), response.length);
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

    SupplyLineDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supplyLineId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(SupplyLineDto.class);

    assertEquals(supplyLineDto, response);
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

  private SupplyLine generateSupplyLine() {
    SupplyLine supplyLine = new SupplyLine();
    supplyLine.setProgram(generateProgram());
    supplyLine.setSupervisoryNode(generateSupervisoryNode());
    supplyLine.setSupplyingFacility(generateFacility());
    return supplyLine;
  }

  private SupervisoryNode generateSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode("234");
    supervisoryNode.setFacility(generateFacility());
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new Program("890");
    program.setPeriodsSkippable(false);
    return program;
  }

  private Facility generateFacility() {
    Integer instanceNumber = +generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility("FacilityCode" + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("FacilityName" + instanceNumber);
    facility.setDescription("FacilityDescription" + instanceNumber);
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel" + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone" + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType" + generateInstanceNumber());
    return facilityType;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
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
