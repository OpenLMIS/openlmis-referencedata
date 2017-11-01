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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.MinimalFacilityDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductsDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyLineDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String PROGRAM_ID = "programId";
  private static final String SUPERVISORY_NODE_ID = "supervisoryNodeId";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String MINIMAL_URL = RESOURCE_URL + "/minimal";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SUPPLYING_URL = RESOURCE_URL + "/supplying";
  private static final String SEARCH_FACILITIES = RESOURCE_URL + "/search";
  private static final String BYBOUNDARY_URL = RESOURCE_URL + "/byBoundary";
  private static final String NAME_KEY = "name";

  private UUID programId;
  private UUID supervisoryNodeId;
  private Program program;
  private Facility facility;
  private GeometryFactory gf = new GeometryFactory();
  private Coordinate[] coords = new Coordinate[] {
      new Coordinate(0, 0),
      new Coordinate(2, 0),
      new Coordinate(2, 2),
      new Coordinate(0, 2),
      new Coordinate(0, 0)
  };

  @Before
  public void setUp() {
    programId = UUID.randomUUID();
    program = new ProgramDataBuilder().withId(programId).build();
    facility = new FacilityDataBuilder()
        .withSupportedProgram(program).build();
  }

  @Test
  public void shouldReturnSupplyingDepots() {

    int searchedFacilitiesAmt = 3;

    SupervisoryNode searchedSupervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .build();

    List<SupplyLine> searchedSupplyLines =
        generateSupplyLines(searchedFacilitiesAmt, searchedSupervisoryNode);

    given(programRepository.findOne(programId)).willReturn(program);
    given(supervisoryNodeRepository.findOne(searchedSupervisoryNode.getId()))
        .willReturn(searchedSupervisoryNode);
    given(supplyLineService.searchSupplyLines(program, searchedSupervisoryNode))
        .willReturn(searchedSupplyLines);

    FacilityDto[] response = restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam(SUPERVISORY_NODE_ID, searchedSupervisoryNode.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    List<Facility> expectedFacilities = searchedSupplyLines.stream()
        .map(SupplyLine::getSupplyingFacility).distinct().collect(Collectors.toList());

    assertEquals(expectedFacilities.size(), response.length);

    for (FacilityDto facilityDto : response) {
      Facility facility = Facility.newFacility(facilityDto);
      assertTrue(expectedFacilities.contains(facility));
    }
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingSupervisorNode() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    supervisoryNodeId = UUID.randomUUID();

    given(programRepository.findOne(programId)).willReturn(program);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(null);

    restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam(SUPERVISORY_NODE_ID, supervisoryNodeId)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingProgram() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    SupervisoryNode searchedSupervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .build();

    given(programRepository.findOne(programId)).willReturn(null);
    given(supervisoryNodeRepository.findOne(searchedSupervisoryNode.getId()))
        .willReturn(searchedSupervisoryNode);

    restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam(SUPERVISORY_NODE_ID, searchedSupervisoryNode.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    String similarCode = "Facility";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("code", similarCode);

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityService.searchFacilities(requestBody))
        .willReturn(listToReturn);

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    Map<String, String> foundFacility = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(facility.getCode(), foundFacility.get("code"));
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {

    String similarName = "Facility";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(NAME_KEY, similarName);

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityService.searchFacilities(requestBody))
        .willReturn(listToReturn);

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    Map<String, String> foundFacility = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(facility.getName(), foundFacility.get(NAME_KEY));
  }

  @Test
  public void shouldReturnBadRequestWhenSearchThrowsException() {
    // given
    given(facilityService.searchFacilities(anyMap())).willThrow(
        new ValidationMessageException("somethingWrong"));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(new HashMap<>())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotFindFacilitiesWithIncorrectCodeAndName() {

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("code", "IncorrectCode");
    requestBody.put(NAME_KEY, "NotSimilarName");

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(0, response.getContent().size());
  }

  @Test
  public void shouldPaginateSearchFacilities() {

    Map<String, Object> requestBody = new HashMap<>();

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityService.searchFacilities(requestBody))
        .willReturn(listToReturn);

    PageImplRepresentation response = restAssured.given()
        .queryParam("page", 0)
        .queryParam("size", 1)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(1, response.getSize());
    assertEquals(0, response.getNumber());
  }

  @Test
  public void shouldFindApprovedProductsForFacility() {

    when(facilityRepository.findOne(any(UUID.class))).thenReturn(facility);
    when(facilityTypeApprovedProductRepository.searchProducts(any(UUID.class), any(UUID.class),
        eq(false))).thenReturn(Collections.singletonList(
            new FacilityTypeApprovedProductsDataBuilder().build()));

    List<Map<String, ?>> productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(200)
        .extract().as(List.class);

    assertEquals(1, productDtos.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestWhenLookingForProductsInNonExistantFacility() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    when(facilityRepository.findOne(any(UUID.class))).thenReturn(null);

    restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchWithEmptyParamsWhenNoParamsProvided() {
    List<Facility> storedFacilities = Arrays.asList(facility, new FacilityDataBuilder()
        .withSupportedProgram(program).build());
    given(facilityService.getFacilities(new LinkedMultiValueMap<>()))
        .willReturn(storedFacilities);

    FacilityDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    List<FacilityDto> facilities = Arrays.asList(response);
    assertThat(facilities.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void shouldSearchWithParamsWhenParamsProvided() {
    List<Facility> storedFacilities = Arrays.asList(facility, new FacilityDataBuilder()
        .withSupportedProgram(program).build());
    MultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    UUID facilityIdOne = UUID.randomUUID();
    UUID facilityIdTwo = UUID.randomUUID();
    queryMap.add("id", facilityIdOne.toString());
    queryMap.add("id", facilityIdTwo.toString());

    given(facilityService.getFacilities(queryMap)).willReturn(storedFacilities);

    FacilityDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("id", facilityIdOne)
        .queryParam("id", facilityIdTwo)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    List<FacilityDto> facilities = Arrays.asList(response);
    assertThat(facilities.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getAllShouldGetAllFacilitiesWithMinimalRepresentation() {
    List<Facility> storedFacilities = Arrays.asList(facility, new FacilityDataBuilder()
        .withSupportedProgram(program).build());
    given(facilityRepository.findAll()).willReturn(storedFacilities);

    MinimalFacilityDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(MinimalFacilityDto[].class);

    List<MinimalFacilityDto> facilities = Arrays.asList(response);
    assertThat(facilities.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getShouldGetFacility() {
    given(facilityRepository.findOne(any(UUID.class))).willReturn(facility);

    FacilityDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto.class);

    assertEquals(facility.getCode(), response.getCode());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getShouldReturnNotFoundForNonExistingFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldDeleteFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(facility);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnForbiddenForUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.FACILITIES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  @Test
  public void getFacilityAuditLogShouldReturnNotFoundIfFacilityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  @Test
  public void deleteShouldReturnNotFoundForNonExistingFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postShouldCreateFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    given(programRepository.findByCode(any(Code.class))).willReturn(program);
    given(facilityRepository.save(facility)).willReturn(facility);

    FacilityDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(FacilityDto.class);

    assertEquals(facilityDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postShouldReturnBadRequestForNonExistentSupportedProgram() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    given(programRepository.findByCode(any(Code.class))).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.FACILITIES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldSaveFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    given(programRepository.findByCode(any(Code.class))).willReturn(program);
    given(facilityRepository.saveAndFlush(facility)).willReturn(facility);

    FacilityDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto.class);

    assertEquals(facilityDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnBadRequestForNonExistentSupportedProgram() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    given(programRepository.findByCode(any(Code.class))).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.FACILITIES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSaveFacilityWithNullSupportedPrograms() {
    testSaveForSupportedPrograms(null);
  }

  @Test
  public void shouldSaveFacilityWithEmptySupportedPrograms() {
    testSaveForSupportedPrograms(Collections.emptySet());
  }

  private void testSaveForSupportedPrograms(Set<SupportedProgram> programs) {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);

    facilityDto.setSupportedPrograms(programs);

    when(facilityRepository.saveAndFlush(any(Facility.class))).thenReturn(facility);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200);

    verify(facilityRepository).saveAndFlush(facility);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findByBoundaryShouldFindFacilities() {

    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    Polygon boundary = gf.createPolygon(coords);
    given(facilityRepository.findByBoundary(boundary))
        .willReturn(Collections.singletonList(facility));

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(boundary)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(BYBOUNDARY_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    Map<String, String> foundFacility = (Map) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(facility.getName(), foundFacility.get(NAME_KEY));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findByBoundaryShouldReturnForbiddenForUnauthorizedToken() {

    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    Polygon boundary = gf.createPolygon(coords);

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(boundary)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(BYBOUNDARY_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(facility);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private List<SupplyLine> generateSupplyLines(int searchedFacilitiesAmt,
                                               SupervisoryNode searchedSupervisoryNode) {
    List<SupplyLine> searchedSupplyLines = new ArrayList<>();
    for (int i = 0; i < searchedFacilitiesAmt; i++) {
      SupplyLine supplyLine = new SupplyLineDataBuilder()
          .withProgram(program)
          .withSupplyingFacility(facility)
          .withSupervisoryNode(searchedSupervisoryNode)
          .build();
      searchedSupplyLines.add(supplyLine);
    }
    return searchedSupplyLines;
  }
}
