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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;

import org.hamcrest.Matchers;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.CurrencyConfig;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String PROGRAM_ID = "programId";
  static final String SUPERVISORY_NODE_ID = "supervisoryNodeId";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_URL = ID_URL + "/auditLog";
  private static final String SUPPLYING_URL = RESOURCE_URL + "/supplying";
  private static final String FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME =
      RESOURCE_URL + "/search";

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private SupplyLineService supplyLineService;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private FacilityTypeApprovedProductRepository facilityTypeApprovedProductRepository;

  @MockBean
  private SupervisoryNodeRepository supervisoryNodeRepository;

  private Integer currentInstanceNumber;
  private UUID programId;
  private UUID supervisoryNodeId;
  private Program program;
  private Facility facility;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
    program = generateProgram();
    programId = UUID.randomUUID();
    facility = generateFacility();
  }

  @Test
  public void shouldReturnSupplyingDepots() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    int searchedFacilitiesAmt = 3;

    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();

    List<SupplyLine> searchedSupplyLines = new ArrayList<>();
    for (int i = 0; i < searchedFacilitiesAmt; i++) {
      SupplyLine supplyLine = generateSupplyLine();
      supplyLine.setProgram(program);
      supplyLine.setSupervisoryNode(searchedSupervisoryNode);

      searchedSupplyLines.add(supplyLine);
    }

    given(programRepository.findOne(programId)).willReturn(program);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);
    given(supplyLineService.searchSupplyLines(program, searchedSupervisoryNode))
        .willReturn(searchedSupplyLines);

    FacilityDto[] response = restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam(SUPERVISORY_NODE_ID, supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldRejectGetSupplingRequestIfUserHasNoRight() {
    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    int searchedFacilitiesAmt = 3;

    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();

    List<SupplyLine> searchedSupplyLines = new ArrayList<>();
    for (int i = 0; i < searchedFacilitiesAmt; i++) {
      SupplyLine supplyLine = generateSupplyLine();
      supplyLine.setProgram(program);
      supplyLine.setSupervisoryNode(searchedSupervisoryNode);

      searchedSupplyLines.add(supplyLine);
    }

    given(programRepository.findOne(programId)).willReturn(program);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);
    given(supplyLineService.searchSupplyLines(program, searchedSupervisoryNode))
        .willReturn(searchedSupplyLines);

    String messageKey = restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam(SUPERVISORY_NODE_ID, supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
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
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingProgram() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();

    given(programRepository.findOne(programId)).willReturn(null);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);

    restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam(SUPERVISORY_NODE_ID, supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    String similarCode = "Facility";
    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityRepository.findFacilitiesByCodeOrName(similarCode, null))
        .willReturn(listToReturn);

    FacilityDto[] response = restAssured.given()
        .queryParam("code", similarCode)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    List<FacilityDto> facilities = Arrays.asList(response);
    assertEquals(1, facilities.size());
    assertEquals(facility.getCode(), facilities.get(0).getCode());
  }

  @Test
  public void shouldRejectSearchRequestIfUserHasNoRight() {
    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    String messageKey = restAssured.given()
        .queryParam("code", "Facility")
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    String similarName = "Facility";
    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(facility);
    given(facilityRepository.findFacilitiesByCodeOrName(null, similarName))
        .willReturn(listToReturn);

    FacilityDto[] response = restAssured.given()
        .queryParam("name", similarName)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    List<FacilityDto> facilities = Arrays.asList(response);
    assertEquals(1, facilities.size());
    assertEquals(facility.getName(), facilities.get(0).getName());
  }

  @Test
  public void shouldNotFindFacilitiesWithIncorrectCodeAndName() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    Facility[] response = restAssured.given()
        .queryParam("code", "IncorrectCode")
        .queryParam("name", "NotSimilarName")
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    List<Facility> facilities = Arrays.asList(response);
    assertEquals(0, facilities.size());
  }

  @Test
  public void shouldFindApprovedProductsForFacility() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    when(facilityRepository.findOne(any(UUID.class))).thenReturn(facility);
    when(facilityTypeApprovedProductRepository.searchProducts(any(UUID.class), any(UUID.class),
        eq(false))).thenReturn(generateFacilityTypeApprovedProducts());

    List<Map<String, ?>> productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(200)
        .extract().as(List.class);

    assertEquals(1, productDtos.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetApprovedProductsRequestIfUserHasNoRight() {
    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    String messageKey = restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestWhenLookingForProductsInNonExistantFacility() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    when(facilityRepository.findOne(any(UUID.class))).thenReturn(null);

    restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldGetAllFacilities() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    Set<Facility> storedFacilities = Sets.newHashSet(facility, generateFacility());
    given(facilityRepository.findAll()).willReturn(storedFacilities);

    FacilityDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    List<FacilityDto> facilities = Arrays.asList(response);
    assertThat(facilities.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.FACILITIES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldGetFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(facility);

    FacilityDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto.class);

    assertEquals(facility.getCode(), response.getCode());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.FACILITIES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnNotFoundForNonExistingFacility() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityRepository.findOne(any(UUID.class))).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
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

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(AUDIT_URL)
        .then()
        .statusCode(404);

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
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
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
    given(facilityRepository.save(facility)).willReturn(facility);

    FacilityDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
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
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private SupplyLine generateSupplyLine() {
    SupplyLine supplyLine = new SupplyLine();
    supplyLine.setProgram(program);
    supplyLine.setSupervisoryNode(generateSupervisoryNode());
    supplyLine.setSupplyingFacility(facility);
    return supplyLine;
  }

  private SupervisoryNode generateSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNodeId = UUID.randomUUID();
    supervisoryNode.setId(supervisoryNodeId);
    supervisoryNode.setCode("SupervisoryNode " + generateInstanceNumber());
    supervisoryNode.setFacility(facility);
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new Program("Program " + generateInstanceNumber());
    programId = UUID.randomUUID();
    program.setId(programId);
    program.setPeriodsSkippable(false);
    return program;
  }

  private Facility generateFacility() {
    Integer instanceNumber = generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility("FacilityCode " + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("FacilityName " + instanceNumber);
    facility.setDescription("FacilityDescription " + instanceNumber);
    facility.setEnabled(true);
    facility.setActive(true);
    facility.setGoLiveDate(LocalDate.now());
    facility.setGoDownDate(LocalDate.now().plusMonths(1));
    SupportedProgram supportedProgram = SupportedProgram.newSupportedProgram(facility,
        program, true, LocalDate.now());
    facility.addSupportedProgram(supportedProgram);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel " + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone " + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType " + generateInstanceNumber());
    return facilityType;
  }

  private List<FacilityTypeApprovedProduct> generateFacilityTypeApprovedProducts() {
    OrderableDisplayCategory category = OrderableDisplayCategory.createNew(Code.code("gloves"));
    category.setId(UUID.randomUUID());
    Orderable orderable = CommodityType.newCommodityType(
        "gloves", "pair", "Gloves", "testDesc", 6, 3, false);
    orderable.setId(UUID.randomUUID());
    CurrencyUnit currencyUnit = CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE);
    ProgramOrderable programOrderable = ProgramOrderable.createNew(program, category,
        orderable, 0, true, false, 0, Money.of(currencyUnit, 0),
        currencyUnit);
    programOrderable.setId(UUID.randomUUID());
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setProgramOrderable(programOrderable);
    ftap.setId(UUID.randomUUID());
    ftap.setMinPeriodsOfStock(1d);
    ftap.setMaxPeriodsOfStock(3d);
    ftap.setFacilityType(generateFacilityType());
    ftap.setEmergencyOrderPoint(1d);
    List<FacilityTypeApprovedProduct> products = new ArrayList<>();
    products.add(ftap);
    return products;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
