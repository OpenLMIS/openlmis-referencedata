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
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.openlmis.referencedata.domain.RightName.REQUISITION_GROUPS_MANAGE;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.RequisitionGroupBaseDto;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.service.RequisitionGroupService;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.validate.RequisitionGroupValidator;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import guru.nidi.ramltester.junit.RamlMatchers;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
public class RequisitionGroupControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/requisitionGroups";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DESCRIPTION = "OpenLMIS";

  @MockBean
  private RequisitionGroupRepository requisitionGroupRepository;

  @MockBean
  private RequisitionGroupService requisitionGroupService;

  @MockBean
  private RequisitionGroupValidator requisitionGroupValidator;

  private RequisitionGroup requisitionGroup;
  private RequisitionGroupBaseDto requisitionGroupDto;
  private UUID requisitionGroupId;

  private SupervisoryNode supervisoryNode;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private Program program;
  private ProcessingSchedule processingSchedule;

  /**
   * Constructor for tests.
   */
  public RequisitionGroupControllerIntegrationTest() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("FO1");

    FacilityType facilityType = new FacilityType("FT1");

    GeographicLevel geoLevel = new GeographicLevel("GL1", 1);

    GeographicZone geoZone = new GeographicZone("GZ1", geoLevel);

    Facility facility = new Facility("F1");
    facility.setActive(true);
    facility.setGeographicZone(geoZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("SN1", facility);
    supervisoryNode.setId(UUID.randomUUID());

    program = new Program("PRO-1");
    processingSchedule = new ProcessingSchedule("SCH-1", "Monthly Schedule");

    requisitionGroup = new RequisitionGroup("RG1", "Requisition Group 1", supervisoryNode);
    supervisoryNode.setRequisitionGroup(requisitionGroup);

    requisitionGroupProgramSchedule = RequisitionGroupProgramSchedule
        .newRequisitionGroupProgramSchedule(requisitionGroup, program, processingSchedule, true);
    requisitionGroupProgramSchedule.setId(UUID.randomUUID());
    List<RequisitionGroupProgramSchedule> schedules = new ArrayList<>();
    schedules.add(requisitionGroupProgramSchedule);

    requisitionGroup.setRequisitionGroupProgramSchedules(schedules);

    requisitionGroupId = UUID.randomUUID();
    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);
  }

  @Test
  public void shouldDeleteRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requisitionGroupDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requisitionGroupDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllRequisitionGroups() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    List<RequisitionGroup> storedRequisitionGroups = Arrays.asList(requisitionGroup,
        new RequisitionGroup("RG2", "Requisition Group 2", supervisoryNode));
    given(requisitionGroupRepository.findAll()).willReturn(storedRequisitionGroups);

    RequisitionGroupDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto[].class);

    assertEquals(storedRequisitionGroups.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetAllRequisitionGroupsIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);
    given(requisitionGroupRepository.save(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    RequisitionGroupDto requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .body(requisitionGroupDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);
    given(requisitionGroupRepository.save(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    RequisitionGroupDto requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .body(requisitionGroupDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRequisitionGroupIfDoesNotExist() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(null);
    given(requisitionGroupRepository.save(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    RequisitionGroupDto requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .body(requisitionGroupDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindRequisitionGroupsWithSimilarCode() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    String similarCode = "RG";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("code", similarCode);

    List<RequisitionGroup> listToReturn = new ArrayList<>();
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(0);
    given(pageable.getPageSize()).willReturn(1);
    listToReturn.add(requisitionGroup);
    given(requisitionGroupService.searchRequisitionGroups(eq(requestBody), any(Pageable.class)))
        .willReturn(Pagination.getPage(listToReturn, null, 1));

    PageImplRepresentation response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    Map<String, String> foundRequisitionGroup = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(requisitionGroup.getCode(), foundRequisitionGroup.get("code"));
  }

  @Test
  public void shouldFindRequisitionGroupsWithSimilarName() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    String similarName = "group-name";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("name", similarName);

    List<RequisitionGroup> listToReturn = new ArrayList<>();
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(0);
    given(pageable.getPageSize()).willReturn(1);
    listToReturn.add(requisitionGroup);
    given(requisitionGroupService.searchRequisitionGroups(eq(requestBody), any(Pageable.class)))
        .willReturn(Pagination.getPage(listToReturn, null, 1));

    PageImplRepresentation response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    Map<String, String> foundRequisitionGroup = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(requisitionGroup.getCode(), foundRequisitionGroup.get("code"));
  }

  @Test
  public void shouldRejectSearchRequestIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    String messageKey = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(new HashMap<>())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchThrowsException() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupService.searchRequisitionGroups(anyMap(), any(Pageable.class))).willThrow(
        new ValidationMessageException("somethingWrong"));

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(new HashMap<>())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPaginateSearchFacilities() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    List<RequisitionGroup> listToReturn = new ArrayList<>();
    listToReturn.add(requisitionGroup);

    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(0);
    given(pageable.getPageSize()).willReturn(1);

    Map<String, Object> requestBody = new HashMap<>();

    given(requisitionGroupService.searchRequisitionGroups(eq(requestBody), any(Pageable.class)))
        .willReturn(Pagination.getPage(listToReturn, null, 1));

    PageImplRepresentation response = restAssured.given()
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam(ACCESS_TOKEN, getToken())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
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

}
