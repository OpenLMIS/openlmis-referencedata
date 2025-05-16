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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProcessingScheduleDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.service.PageDto;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingScheduleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingSchedules";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  private ProcessingSchedule schedule;
  private Facility facility;
  private Program program;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private UUID processingScheduleId;
  private UUID facilityId;
  private UUID programId;

  /**
   * Test class constructor.
   */
  public ProcessingScheduleControllerIntegrationTest() {
    schedule = new ProcessingSchedule(Code.code("PS1"), "Schedule1");
    schedule.setId(UUID.randomUUID());
    program = new Program("PRO-1");
    facility = new Facility("FAC-1");
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    requisitionGroupProgramSchedule.setProcessingSchedule(schedule);
    processingScheduleId = UUID.randomUUID();
    facilityId = UUID.randomUUID();
    programId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteSchedule() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findById(processingScheduleId)).willReturn(Optional.of(schedule));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostProcessingSchedule() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingSchedule response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(schedule)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ProcessingSchedule.class);

    assertEquals(schedule, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutProcessingSchedule() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    schedule.setDescription("OpenLMIS");
    given(scheduleRepository.findById(processingScheduleId)).willReturn(Optional.of(schedule));

    ProcessingSchedule response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .body(schedule)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule.class);

    assertEquals(schedule, response);
    assertEquals("OpenLMIS", response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllProcessingSchedules() {
    ProcessingScheduleDto dto = new ProcessingScheduleDto();
    schedule.export(dto);
    PageRequest pageRequest = PageRequest.of(1, 10);
    given(scheduleRepository.findAll(pageRequest))
        .willReturn(Pagination.getPage(Collections.singletonList(schedule), pageRequest, 11));

    PageDto<LinkedHashMap> response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("page", 1)
        .queryParam("size", 10)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(1, response.getContent().size());
    assertEquals(dto.getId().toString(), response.getContent().get(0).get("id").toString());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingSchedule() {

    given(scheduleRepository.findById(processingScheduleId)).willReturn(Optional.of(schedule));

    ProcessingSchedule response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule.class);

    assertEquals(schedule, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingScheduleByFacilityAndProgram() {

    given(facilityRepository.existsById(facilityId)).willReturn(true);
    given(programRepository.existsById(programId)).willReturn(true);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedules(
        programId, facilityId)).willReturn(
            singletonList(requisitionGroupProgramSchedule));

    ProcessingSchedule[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule[].class);

    assertEquals(schedule, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestSearchWhenProgramDoesNotExist() {

    given(facilityRepository.findById(facilityId)).willReturn(Optional.of(facility));
    given(programRepository.findById(programId)).willReturn(Optional.empty());
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedules(
        programId, facilityId)).willReturn(singletonList(requisitionGroupProgramSchedule));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestSearchWhenFacilityDoesNotExist() {

    given(facilityRepository.findById(facilityId)).willReturn(Optional.empty());
    given(programRepository.findById(programId)).willReturn(Optional.of(program));
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedules(
        programId, facilityId)).willReturn(singletonList(requisitionGroupProgramSchedule));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnDeleteScheduleIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnPostProcessingScheduleIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(schedule)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnPutProcessingScheduleIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .body(schedule)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllSchedulesShouldReturnUnauthorizedWithoutAuthorization() {

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
  public void getChosenScheduleShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", processingScheduleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findById(any(UUID.class))).willReturn(Optional.of(schedule));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
