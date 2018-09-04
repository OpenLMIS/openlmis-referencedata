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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.FACILITIES_MANAGE_RIGHT;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import guru.nidi.ramltester.junit.RamlMatchers;
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
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.MinimalFacilityDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductsDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.SupplyLineDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
  private static final String FULL_SUPPLY = "fullSupply";
  private static final String APPROVED_PRODUCTS = "/approvedProducts";

  private UUID programId;
  private UUID orderableId1;
  private UUID orderableId2;
  private UUID supervisoryNodeId;
  private Program program;
  private GeographicZone geographicZone = new GeographicZoneDataBuilder().build();
  private FacilityType facilityType = new FacilityTypeDataBuilder().build();
  private Facility facility;
  private Facility facility1;
  private Program program1;
  private GeometryFactory gf = new GeometryFactory();
  private Coordinate[] coords = new Coordinate[] {
      new Coordinate(0, 0),
      new Coordinate(2, 0),
      new Coordinate(2, 2),
      new Coordinate(0, 2),
      new Coordinate(0, 0)
  };

  @Before
  @Override
  public void setUp() {
    super.setUp();

    programId = UUID.randomUUID();
    orderableId1 = UUID.randomUUID();
    orderableId2 = UUID.randomUUID();
    program = new ProgramDataBuilder().withId(programId).build();
    program1 = new ProgramDataBuilder().withId(programId).build();
    facility = new FacilityDataBuilder()
        .withSupportedProgram(program)
        .withGeographicZone(geographicZone)
        .withType(facilityType)
        .withoutOperator()
        .build();
    facility1 = new FacilityDataBuilder()
        .withSupportedProgram(program)
        .withSupportedProgram(program1)
        .withGeographicZone(geographicZone)
        .withType(facilityType)
        .withoutOperator()
        .build();

    mockUserHasRight(FACILITIES_MANAGE_RIGHT);

    given(geographicZoneRepository.findOne(geographicZone.getId())).willReturn(geographicZone);
    given(facilityTypeRepository.findOne(facilityType.getId())).willReturn(facilityType);
    given(facilityRepository.findOne(facility.getId())).willReturn(facility);
    given(facilityRepository.findOne(facility1.getId())).willReturn(facility1);
  }

  @Test
  public void shouldReturnSupplyingDepots() {

    int searchedFacilitiesAmt = 3;

    SupervisoryNode searchedSupervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .build();

    List<SupplyLine> searchedSupplyLines =
        generateSupplyLines(searchedFacilitiesAmt, searchedSupervisoryNode);
    List<Facility> facilities = searchedSupplyLines
        .stream()
        .map(SupplyLine::getSupplyingFacility)
        .distinct()
        .collect(Collectors.toList());

    given(programRepository.exists(programId)).willReturn(true);
    given(supervisoryNodeRepository.exists(searchedSupervisoryNode.getId()))
        .willReturn(true);
    given(supplyLineRepository.findSupplyingFacilities(programId, searchedSupervisoryNode.getId()))
        .willReturn(facilities);

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

    assertThat(response.length, is(facilities.size()));

    Facility[] responseFacilities = Arrays
        .stream(response)
        .map(Facility::newFacility)
        .toArray(Facility[]::new);

    assertThat(facilities, hasItems(responseFacilities));
  }

  @Test
  public void getSupplyingDepotsShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam(SUPERVISORY_NODE_ID, UUID.randomUUID())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingSupervisorNode() {
    supervisoryNodeId = UUID.randomUUID();

    given(programRepository.exists(programId)).willReturn(true);
    given(supervisoryNodeRepository.exists(supervisoryNodeId)).willReturn(false);

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
    SupervisoryNode searchedSupervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .build();

    given(programRepository.exists(programId)).willReturn(false);

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
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    requestBody.entrySet().stream()
        .forEach(entry -> map.add(entry.getKey(), entry.getValue()));

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityService.searchFacilities(
        new FacilitySearchParams(map)))
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
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .body(new HashMap<>())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {

    String similarName = "Facility";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(NAME_KEY, similarName);
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    requestBody.entrySet().stream()
        .forEach(entry -> map.add(entry.getKey(), entry.getValue()));

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityService.searchFacilities(new FacilitySearchParams(map)))
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
    given(facilityService.searchFacilities(any())).willThrow(
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

    MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityService.searchFacilities(new FacilitySearchParams(requestBody)))
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
    pageable = new PageRequest(0, Integer.MAX_VALUE);
    when(facilityRepository.findOne(any(UUID.class))).thenReturn(facility);
    when(facilityTypeApprovedProductRepository.searchProducts(any(UUID.class), any(UUID.class),
        eq(false), eq(null), eq(pageable)))
        .thenReturn(new PageImpl<>(Collections.singletonList(
            new FacilityTypeApprovedProductsDataBuilder().build()), pageable, 1));

    PageImplRepresentation productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam(FULL_SUPPLY, false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + APPROVED_PRODUCTS)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, productDtos.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindApprovedProductsForFacilityAndOrderableIds() {
    pageable = new PageRequest(0, Integer.MAX_VALUE);
    List<UUID> orderableIds = asList(orderableId1, orderableId2);

    when(facilityRepository.findOne(any(UUID.class))).thenReturn(facility);
    when(facilityTypeApprovedProductRepository.searchProducts(any(UUID.class), any(UUID.class),
        eq(false), eq(orderableIds), eq(pageable)))
        .thenReturn(new PageImpl<>(Collections.singletonList(
            new FacilityTypeApprovedProductsDataBuilder().build()), pageable, 1));

    PageImplRepresentation productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam(FULL_SUPPLY, false)
        .queryParam("orderableId", orderableId1)
        .queryParam("orderableId", orderableId2)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + APPROVED_PRODUCTS)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, productDtos.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getApprovedProductsShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam(FULL_SUPPLY, false)
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + APPROVED_PRODUCTS)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestWhenLookingForProductsInNonExistantFacility() {
    when(facilityRepository.findOne(any(UUID.class))).thenReturn(null);

    restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam(FULL_SUPPLY, false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + APPROVED_PRODUCTS)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchWithEmptyParamsWhenNoParamsProvided() {
    List<Facility> storedFacilities = asList(facility, new FacilityDataBuilder()
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

    List<FacilityDto> facilities = asList(response);
    assertThat(facilities.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void shouldSearchWithParamsWhenParamsProvided() {
    List<Facility> storedFacilities = asList(facility, new FacilityDataBuilder()
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

    List<FacilityDto> facilities = asList(response);
    assertThat(facilities.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void shouldReturnInactiveFacilitiesWithMinimalRepresentation() {
    facility = new FacilityDataBuilder()
        .withSupportedProgram(program).nonActive().build();
    given(facilityRepository.findByActive(eq(false), any(Pageable.class))).willReturn(
        Pagination.getPage(Lists.newArrayList(facility)));

    PageImplRepresentation response = restAssured
        .given()
        .queryParam("active", false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(response.getContent().size(), 1);
    assertEquals(response.getNumberOfElements(), 1);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void shouldReturnActiveFacilitiesWithMinimalRepresentation() {
    given(facilityRepository.findByActive(eq(true), any(Pageable.class))).willReturn(
        Pagination.getPage(Lists.newArrayList(facility)));

    PageImplRepresentation response = restAssured
        .given()
        .queryParam("active", true)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(response.getContent().size(), 1);
    assertEquals(response.getNumberOfElements(), 1);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getAllShouldGetAllFacilitiesWithMinimalRepresentation() {
    List<Facility> storedFacilities = asList(facility, new FacilityDataBuilder()
        .withSupportedProgram(program).build());
    given(facilityRepository.findAll(any(Pageable.class))).willReturn(
        Pagination.getPage(storedFacilities));

    Page<MinimalFacilityDto> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(storedFacilities.size(), response.getContent().size());
    assertEquals(storedFacilities.size(), response.getNumberOfElements());
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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
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
    mockUserHasNoRight(FACILITIES_MANAGE_RIGHT);

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
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  @Test
  public void deleteShouldReturnNotFoundForNonExistingFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
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
    mockUserHasNoRight(FACILITIES_MANAGE_RIGHT);
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
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    given(programRepository.findByCode(any(Code.class))).willReturn(program);
    given(facilityRepository.saveAndFlush(facility)).willReturn(facility);

    FacilityDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", facilityDto.getId())
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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    given(programRepository.findByCode(any(Code.class))).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", facilityDto.getId())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnBadRequestForDuplicateProgramSupported() {
    doNothing()
            .when(rightService)
            .checkAdminRight(FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility1.export(facilityDto);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam("id", facilityDto.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(facilityDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(400)
            .body(MESSAGE_KEY, is(FacilityMessageKeys.ERROR_DUPLICATE_PROGRAM_SUPPORTED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnBadRequestIfIdMismatch() {
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
        .statusCode(400)
        .body(MESSAGE_KEY, is(FacilityMessageKeys.ERROR_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnForbiddenForUnauthorizedToken() {
    mockUserHasNoRight(FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", facilityDto.getId())
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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);

    facilityDto.setSupportedPrograms(programs);

    when(facilityRepository.saveAndFlush(any(Facility.class))).thenReturn(facility);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", facilityDto.getId())
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

    mockUserHasNoRight(FACILITIES_MANAGE_RIGHT);

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
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    mockUserHasNoRight(FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
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
