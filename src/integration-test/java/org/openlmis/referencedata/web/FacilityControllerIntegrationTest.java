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
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.nullable;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.MinimalFacilityDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductsDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
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

  public static final String SIZE = "size";
  private static final String PROGRAM_ID = "programId";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String MINIMAL_URL = RESOURCE_URL + "/minimal";
  private static final String FULL_URL = RESOURCE_URL + "/full";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_FACILITIES = RESOURCE_URL + "/search";
  private static final String BYBOUNDARY_URL = RESOURCE_URL + "/byBoundary";
  private static final String NAME_KEY = "name";
  private static final String FULL_SUPPLY = "fullSupply";
  private static final String APPROVED_PRODUCTS = "/approvedProducts";
  public static final String PAGE = "page";
  public static final String CODE = "code";

  private UUID programId;
  private UUID orderableId;
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
  private PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);

  @Before
  @Override
  public void setUp() {
    super.setUp();

    programId = UUID.randomUUID();
    orderableId = UUID.randomUUID();
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

    given(geographicZoneRepository.findById(geographicZone.getId()))
        .willReturn(Optional.of(geographicZone));
    given(facilityTypeRepository.findById(facilityType.getId()))
        .willReturn(Optional.of(facilityType));
    given(facilityRepository.findById(facility.getId())).willReturn(Optional.of(facility));
    given(facilityRepository.findById(facility1.getId())).willReturn(Optional.of(facility1));
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    String similarCode = "Facility";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(CODE, similarCode);
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    requestBody.entrySet().stream()
        .forEach(entry -> map.add(entry.getKey(), entry.getValue()));

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    Page page = Pagination.getPage(listToReturn, pageable, 1);
    given(facilityService.searchFacilities(
        new FacilitySearchParams(map), pageable))
        .willReturn(page);

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    Map<String, String> foundFacility = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(facility.getCode(), foundFacility.get(CODE));
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
    Page page = Pagination.getPage(listToReturn, pageable, 1);
    given(facilityService.searchFacilities(new FacilitySearchParams(map), pageable))
        .willReturn(page);

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    Map<String, String> foundFacility = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(facility.getName(), foundFacility.get(NAME_KEY));
  }

  @Test
  public void shouldReturnBadRequestWhenSearchThrowsException() {
    // given
    given(facilityService.searchFacilities(any(), any())).willThrow(
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
    requestBody.put(CODE, "IncorrectCode");
    requestBody.put(NAME_KEY, "NotSimilarName");

    MultiValueMap<String, Object> transformedRequestBody = new LinkedMultiValueMap<>();
    transformedRequestBody.add(CODE, "IncorrectCode");
    transformedRequestBody.add(NAME_KEY, "NotSimilarName");


    Page page = Pagination.getPage(Collections.emptyList(), pageable, 0);
    given(facilityService.searchFacilities(
            new FacilitySearchParams(transformedRequestBody), pageable))
              .willReturn(page);

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(0, response.getContent().size());
  }

  @Test
  public void shouldPaginateSearchFacilities() {

    MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    Page page = Pagination.getPage(listToReturn, pageable, 1);
    given(facilityService.searchFacilities(new FacilitySearchParams(requestBody), pageable))
        .willReturn(page);

    PageDto response = restAssured.given()
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_FACILITIES)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(Integer.MAX_VALUE, response.getSize());
    assertEquals(0, response.getNumber());
  }

  @Test
  public void shouldFindApprovedProductsForFacility() {
    pageable = PageRequest.of(0, Integer.MAX_VALUE);
    Orderable orderable = new OrderableDataBuilder().build();
    FacilityTypeApprovedProduct approvedProduct = new FacilityTypeApprovedProductsDataBuilder()
        .withOrderableId(orderable.getId())
        .build();

    when(orderableRepository
        .findAllLatestByIds(eq(singleton(orderable.getId())), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.singletonList(orderable), pageable, 1));
    when(facilityTypeApprovedProductRepository.searchProducts(
            eq(facility.getId()),
            eq(singleton(program.getId())),
            eq(false),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(pageable)))
        .thenReturn(new PageImpl<>(Collections.singletonList(approvedProduct), pageable, 1));

    PageDto productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, program.getId())
        .queryParam(FULL_SUPPLY, false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + facility.getId() + APPROVED_PRODUCTS)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(1, productDtos.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindApprovedProductsForFacilityAndOrderableIds() {
    pageable = PageRequest.of(0, Integer.MAX_VALUE);
    Orderable orderable = new OrderableDataBuilder().build();
    List<UUID> orderableIds = Arrays.asList(orderable.getId(), orderableId);
    FacilityTypeApprovedProduct approvedProduct = new FacilityTypeApprovedProductsDataBuilder()
        .withOrderableId(orderable.getId())
        .build();

    when(orderableRepository
        .findAllLatestByIds(eq(singleton(orderable.getId())), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.singletonList(orderable), pageable, 1));
    when(facilityTypeApprovedProductRepository.searchProducts(eq(facility.getId()),
        eq(singleton(program.getId())), eq(false), eq(orderableIds), eq(null), eq(null),
        eq(null), eq(pageable)))
        .thenReturn(new PageImpl<>(Collections.singletonList(approvedProduct), pageable, 1));

    PageDto productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, program.getId())
        .queryParam(FULL_SUPPLY, false)
        .queryParam("orderableId", orderable.getId())
        .queryParam("orderableId", orderableId)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL + "/" + facility.getId() + APPROVED_PRODUCTS)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

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
    when(facilityTypeApprovedProductRepository
        .searchProducts(any(UUID.class), nullable(Set.class), nullable(Boolean.class),
            nullable(List.class), nullable(Boolean.class), nullable(String.class),
            nullable(String.class), any(Pageable.class)))
        .thenThrow(new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND));

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
    given(facilityService.searchFacilities(new FacilitySearchParams(new LinkedMultiValueMap<>()),
        pageable)).willReturn(Pagination.getPage(storedFacilities, PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(storedFacilities.size(), response.getContent().size());
    assertEquals(storedFacilities.size(), response.getNumberOfElements());
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

    given(facilityService.searchFacilities(new FacilitySearchParams(queryMap), pageable))
        .willReturn(Pagination.getPage(storedFacilities, PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("id", facilityIdOne)
        .queryParam("id", facilityIdTwo)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(storedFacilities.size(), response.getContent().size());
    assertEquals(storedFacilities.size(), response.getNumberOfElements());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void shouldReturnInactiveFacilitiesWithMinimalRepresentation() {
    facility = new FacilityDataBuilder()
        .withSupportedProgram(program).nonActive().build();
    given(facilityRepository.findByActive(eq(false), any(Pageable.class))).willReturn(
        Pagination.getPage(Lists.newArrayList(facility), PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .queryParam("active", false)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(response.getContent().size(), 1);
    assertEquals(response.getNumberOfElements(), 1);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void shouldReturnActiveFacilitiesWithMinimalRepresentation() {
    given(facilityRepository.findByActive(eq(true), any(Pageable.class))).willReturn(
        Pagination.getPage(Lists.newArrayList(facility), PageRequest.of(0, 10)));

    PageDto response = restAssured
        .given()
        .queryParam("active", true)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

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
        Pagination.getPage(storedFacilities, PageRequest.of(0, 10)));

    Page<MinimalFacilityDto> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(MINIMAL_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(storedFacilities.size(), response.getContent().size());
    assertEquals(storedFacilities.size(), response.getNumberOfElements());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getFullRepresentationFacilitiesShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
            .when()
            .get(FULL_URL)
            .then()
            .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getFullRepresentationFacilitiesShouldSearchWithEmptyParamsWhenNoParamsProvided() {
    List<Facility> storedFacilities = asList(facility, new FacilityDataBuilder()
            .withSupportedProgram(program).build());
    given(facilityService.searchFacilities(new FacilitySearchParams(new LinkedMultiValueMap<>()),
            pageable)).willReturn(Pagination.getPage(storedFacilities, PageRequest.of(0, 10)));

    PageDto response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .when()
            .get(FULL_URL)
            .then()
            .statusCode(200)
            .extract().as(PageDto.class);

    assertEquals(storedFacilities.size(), response.getContent().size());
    assertEquals(storedFacilities.size(), response.getNumberOfElements());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getFullRepresentationFacilitiesShouldSearchWithParamsWhenParamsProvided() {
    List<Facility> storedFacilities = asList(facility, new FacilityDataBuilder()
            .withSupportedProgram(program).build());
    MultiValueMap<String, Object> queryMap = new LinkedMultiValueMap<>();
    UUID facilityIdOne = UUID.randomUUID();
    UUID facilityIdTwo = UUID.randomUUID();
    queryMap.add("id", facilityIdOne.toString());
    queryMap.add("id", facilityIdTwo.toString());

    given(facilityService.searchFacilities(new FacilitySearchParams(queryMap), pageable))
            .willReturn(Pagination.getPage(storedFacilities, PageRequest.of(0, 10)));

    PageDto response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .queryParam("id", facilityIdOne)
            .queryParam("id", facilityIdTwo)
            .when()
            .get(FULL_URL)
            .then()
            .statusCode(200)
            .extract().as(PageDto.class);

    assertEquals(storedFacilities.size(), response.getContent().size());
    assertEquals(storedFacilities.size(), response.getNumberOfElements());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(rightService);
  }

  @Test
  public void getShouldGetFacility() {
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.of(facility));

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
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.empty());

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
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.of(facility));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    verify(facilityRepository).deleteAll(anyIterable());
    verify(geographicZoneRepository).delete(geographicZone);
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
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  @Test
  public void deleteShouldReturnNotFoundForNonExistingFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.empty());

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
    facilityDto.setId(null);

    given(programRepository.findByCode(any(Code.class))).willReturn(program);
    given(facilityRepository.save(facility)).willReturn(facility);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .body(isEqualToIgnoringGivenFields(facilityDto, ID));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postShouldReturnBadRequestForNonExistentSupportedProgram() {

    doNothing()
        .when(rightService)
        .checkAdminRight(FACILITIES_MANAGE_RIGHT);
    FacilityDto facilityDto = new FacilityDto();
    facility.export(facilityDto);
    facilityDto.setId(null);
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

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(boundary)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(BYBOUNDARY_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

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
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    mockUserHasNoRight(FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    given(facilityRepository.findById(any(UUID.class))).willReturn(Optional.of(facility));

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
