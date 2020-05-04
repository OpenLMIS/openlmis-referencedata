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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openlmis.referencedata.domain.RightName.REQUISITION_GROUPS_MANAGE;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.RequisitionGroupBaseDto;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
public class RequisitionGroupControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/requisitionGroups";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DESCRIPTION = "OpenLMIS";

  private RequisitionGroup requisitionGroup;
  private RequisitionGroupBaseDto requisitionGroupDto;
  private UUID requisitionGroupId;

  private SupervisoryNode supervisoryNode;
  private Facility facility;

  /**
   * Constructor for tests.
   */
  public RequisitionGroupControllerIntegrationTest() {
    facility = new Facility("F1");
    facility.setActive(true);

    GeographicLevel geoLevel = new GeographicLevel("GL1", 1);
    GeographicZone geoZone = new GeographicZoneDataBuilder()
        .withLevel(geoLevel)
        .build();
    facility.setGeographicZone(geoZone);

    FacilityType facilityType = new FacilityType("FT1");
    facility.setType(facilityType);

    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("FO1");
    facility.setOperator(facilityOperator);

    supervisoryNode = new SupervisoryNodeDataBuilder().withFacility(facility).build();
    supervisoryNode.setId(UUID.randomUUID());

    Program program = new Program("PRO-1");
    ProcessingSchedule processingSchedule =
        new ProcessingSchedule(Code.code("SCH-1"), "Monthly Schedule");

    requisitionGroup = new RequisitionGroup("RG1", "Requisition Group 1", supervisoryNode);
    supervisoryNode.setRequisitionGroup(requisitionGroup);

    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            requisitionGroup, program, processingSchedule, true);
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

    given(requisitionGroupRepository.findById(requisitionGroupId))
        .willReturn(Optional.of(requisitionGroup));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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

    given(requisitionGroupRepository.findById(requisitionGroupId))
        .willReturn(Optional.of(requisitionGroup));

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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

    given(requisitionGroupRepository.findById(requisitionGroupId)).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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

    List<RequisitionGroup> storedRequisitionGroups = Arrays.asList(requisitionGroup,
        new RequisitionGroup("RG2", "Requisition Group 2", supervisoryNode));
    given(requisitionGroupRepository.findAll()).willReturn(storedRequisitionGroups);

    RequisitionGroupDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldGetRequisitionGroup() {

    given(requisitionGroupRepository.findById(requisitionGroupId))
        .willReturn(Optional.of(requisitionGroup));

    RequisitionGroupDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void getShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentRequisitionGroup() {

    given(requisitionGroupRepository.findById(requisitionGroupId)).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    given(requisitionGroupRepository.findById(requisitionGroupId))
        .willReturn(Optional.of(requisitionGroup));
    given(requisitionGroupRepository.saveAndFlush(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldUpdateSupervisoryNodeInRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    SupervisoryNode supervisoryNode1 = new SupervisoryNodeDataBuilder()
            .withFacility(facility)
            .withName("Updated SN Name")
            .build();

    assertNotEquals(supervisoryNode, supervisoryNode1);

    assertNotNull(supervisoryNode);
    assertNotNull(supervisoryNode1);

    requisitionGroup.setSupervisoryNode(supervisoryNode1);
    given(requisitionGroupRepository.findById(requisitionGroupId))
        .willReturn(Optional.of(requisitionGroup));
    given(requisitionGroupRepository.saveAndFlush(any(RequisitionGroup.class)))
            .willReturn(requisitionGroup);

    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", requisitionGroupId)
            .body(requisitionGroupDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(200)
            .extract().as(RequisitionGroupDto.class);

    // No supervisory node should ever be changed in the process
    verify(supervisoryNodeRepository, never()).saveAndFlush(any());
    verify(supervisoryNodeRepository, never()).save(any(SupervisoryNode.class));

    assertEquals(requisitionGroupDto, response);
    assertEquals(requisitionGroup.getId(), requisitionGroupDto.getId());
    assertNotNull(requisitionGroupDto.getSupervisoryNode().getId());
    assertEquals(requisitionGroupDto.getSupervisoryNode().getId(), supervisoryNode1.getId());
    assertEquals(requisitionGroupDto.getSupervisoryNode().getName(), "Updated SN Name");
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findById(requisitionGroupId))
        .willReturn(Optional.of(requisitionGroup));
    given(requisitionGroupRepository.saveAndFlush(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    given(requisitionGroupRepository.findById(requisitionGroupId)).willReturn(Optional.empty());
    given(requisitionGroupRepository.saveAndFlush(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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

    String similarCode = "RG";
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("code", similarCode);

    List<RequisitionGroup> listToReturn = new ArrayList<>();
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(0);
    given(pageable.getPageSize()).willReturn(1);
    listToReturn.add(requisitionGroup);
    given(requisitionGroupService.searchRequisitionGroups(eq(requestBody), any(Pageable.class)))
        .willReturn(Pagination.getPage(listToReturn, PageRequest.of(0, 10), 1));

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    Map<String, String> foundRequisitionGroup = (LinkedHashMap) response.getContent().get(0);
    assertEquals(1, response.getContent().size());
    assertEquals(requisitionGroup.getCode(), foundRequisitionGroup.get("code"));
  }

  @Test
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .body(new HashMap<>())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchThrowsException() {

    given(requisitionGroupService.searchRequisitionGroups(anyMap(), any(Pageable.class))).willThrow(
        new ValidationMessageException("somethingWrong"));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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

    List<RequisitionGroup> listToReturn = new ArrayList<>();
    listToReturn.add(requisitionGroup);

    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(0);
    given(pageable.getPageSize()).willReturn(1);

    Map<String, Object> requestBody = new HashMap<>();

    given(requisitionGroupService.searchRequisitionGroups(eq(requestBody), any(Pageable.class)))
        .willReturn(Pagination.getPage(listToReturn, PageRequest.of(0, 10), 1));

    PageDto response = restAssured.given()
        .queryParam("page", 0)
        .queryParam("size", 1)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(1, response.getSize());
    assertEquals(0, response.getNumber());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.REQUISITION_GROUPS_MANAGE);
    given(requisitionGroupRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.REQUISITION_GROUPS_MANAGE);
    given(requisitionGroupRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.REQUISITION_GROUPS_MANAGE);
    given(requisitionGroupRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(requisitionGroup));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}
