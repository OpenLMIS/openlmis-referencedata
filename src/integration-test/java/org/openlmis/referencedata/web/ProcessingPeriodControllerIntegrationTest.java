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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys.ERROR_SCHEDULE_ID_SINGLE_PARAMETER;

import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.dto.ResultDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingPeriodControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingPeriods";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DIFFERENCE_URL = RESOURCE_URL + "/{id}/duration";
  private static final String PROGRAM = "programId";
  private static final String FACILITY = "facilityId";
  private static final String PROCESSING_SCHEDULE = "processingScheduleId";
  private static final String START_DATE = "startDate";
  private static final String END_DATE = "endDate";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String SORT = "sort";
  private static final String CONTENT_SIZE = "content.size()";

  private ProcessingPeriod firstPeriod;
  private ProcessingPeriod secondPeriod;
  private ProcessingPeriod thirdPeriod;

  private ProcessingSchedule schedule;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;

  private UUID firstPeriodId;
  private UUID programId;
  private UUID facilityId;
  private UUID scheduleId;

  private static Integer currentInstanceNumber = 0;

  @Before
  public void setUp() {
    schedule = generateSchedule();
    firstPeriod = ProcessingPeriod.newPeriod("P1", schedule,
        LocalDate.of(2016, 1, 1), LocalDate.of(2016, 2, 1));
    secondPeriod = ProcessingPeriod.newPeriod("P2", schedule,
        LocalDate.of(2016, 2, 2), LocalDate.of(2016, 3, 2));
    thirdPeriod = ProcessingPeriod.newPeriod("P3", schedule,
        LocalDate.of(2016, 3, 3), LocalDate.of(2016, 4, 3));
    requisitionGroupProgramSchedule = generateRequisitionGroupProgramSchedule();
    firstPeriodId = UUID.randomUUID();
    programId = UUID.randomUUID();
    facilityId = UUID.randomUUID();
    scheduleId = UUID.randomUUID();
  }

  @Test
  public void shouldPostPeriodWithoutGap() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);

    ProcessingPeriodDto savedFirstPeriod = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(firstPeriod)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ProcessingPeriodDto.class);

    assertEquals(dto, savedFirstPeriod);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ProcessingPeriodDto dto2 = new ProcessingPeriodDto();
    secondPeriod.export(dto2);

    ProcessingPeriodDto savedSecondPeriod = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(secondPeriod)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ProcessingPeriodDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(dto2, savedSecondPeriod);
  }

  @Test
  public void shouldReturnBadRequestIfThereAreValidationErrorsWhenPostingPeriod() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Errors errors = (Errors) args[1];
      errors.reject("testReject", "rejectMessage");
      return null;
    }).when(periodValidator).validate(anyObject(), any(Errors.class));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(secondPeriod)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfThereAreValidationErrorsWhenPuttingPeriod() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(periodRepository.findById(secondPeriod.getId())).willReturn(Optional.of(secondPeriod));

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Errors errors = (Errors) args[1];
      errors.reject("testReject", "rejectMessage");
      return null;
    }).when(periodValidator).validate(anyObject(), any(Errors.class));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", UUID.randomUUID())
        .body(secondPeriod)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDisplayTotalDifference() {

    given(periodRepository.findById(firstPeriodId)).willReturn(Optional.of(firstPeriod));

    ResultDto<Integer> response = new ResultDto<>();
    response = restAssured.given()
        .pathParam("id", firstPeriodId)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(DIFFERENCE_URL)
        .then()
        .statusCode(200).extract().as(response.getClass());

    assertEquals(1, (int) response.getResult());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchForProcessingPeriods() {
    final UUID id1 = UUID.randomUUID();
    final UUID id2 = UUID.randomUUID();

    given(programRepository.existsById(programId))
        .willReturn(true);
    given(facilityRepository.existsById(facilityId))
        .willReturn(true);
    PageRequest pageable = PageRequest.of(0, 10, Sort.by(START_DATE));
    given(periodRepository
        .search(null, programId, facilityId, firstPeriod.getStartDate(),
            firstPeriod.getEndDate(), asSet(id1, id2), pageable))
        .willReturn(Pagination.getPage(Arrays.asList(firstPeriod, secondPeriod), pageable, 2));

    restAssured.given()
        .queryParam(PROGRAM, programId)
        .queryParam(FACILITY, facilityId)
        .queryParam(START_DATE, firstPeriod.getStartDate().toString())
        .queryParam(END_DATE, firstPeriod.getEndDate().toString())
        .queryParam(ID, id1.toString())
        .queryParam(ID, id2.toString())
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 10)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body(CONTENT_SIZE, is(2));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfProgramIdFacilityIdScheduleIdAreProvided() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM, programId)
        .queryParam(FACILITY, facilityId)
        .queryParam(PROCESSING_SCHEDULE, scheduleId)
        .queryParam(START_DATE, secondPeriod.getStartDate().toString())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY,
            equalTo(ERROR_SCHEDULE_ID_SINGLE_PARAMETER));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldPutPeriod() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    firstPeriod.setDescription("OpenLMIS");
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);

    given(periodRepository.findById(firstPeriodId)).willReturn(Optional.of(firstPeriod));

    ProcessingPeriodDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
        .body(firstPeriod)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto.class);

    assertEquals(response.getDescription(), "OpenLMIS");
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllPeriodsSortedByEndDate() {
    List<ProcessingPeriod> storedPeriods = Lists.newArrayList(
        firstPeriod, secondPeriod, thirdPeriod
    );
    PageRequest pageable = PageRequest.of(0, 10, Sort.by(END_DATE));
    given(periodRepository.search(null,null, null, null, null, emptySet(), pageable))
        .willReturn(Pagination.getPage(storedPeriods, pageable, 3));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 10)
        .queryParam(SORT, END_DATE)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body(CONTENT_SIZE, is(3))
        .body("content[0].startDate", is(firstPeriod.getStartDate().toString()))
        .body("content[1].startDate", is(secondPeriod.getStartDate().toString()))
        .body("content[2].startDate", is(thirdPeriod.getStartDate().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllPeriodsWithDefaultSort() {
    List<ProcessingPeriod> storedPeriods = Lists.newArrayList(
        firstPeriod, secondPeriod, thirdPeriod
    );
    PageRequest pageable = PageRequest.of(0, 10, Sort.by(START_DATE));
    given(periodRepository.search(null,null, null, null, null, emptySet(), pageable))
        .willReturn(Pagination.getPage(storedPeriods, pageable, 3));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 10)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body(CONTENT_SIZE, is(3))
        .body("content[0].startDate", is(firstPeriod.getStartDate().toString()))
        .body("content[1].startDate", is(secondPeriod.getStartDate().toString()))
        .body("content[2].startDate", is(thirdPeriod.getStartDate().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenPeriod() {

    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);
    given(periodRepository.findById(firstPeriodId)).willReturn(Optional.of(firstPeriod));

    ProcessingPeriodDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto.class);

    assertEquals(dto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getDurationShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .pathParam("id", firstPeriodId)
        .when()
        .get(DIFFERENCE_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnPutPeriodIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
        .body(firstPeriod)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllPeriodsShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getChosenPeriodShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
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
    given(periodRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(periodRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(periodRepository.findById(any(UUID.class))).willReturn(Optional.of(firstPeriod));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProcessingSchedule generateSchedule() {
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setId(UUID.randomUUID());
    schedule.setCode(Code.code("S" + generateInstanceNumber()));
    schedule.setName("schedule");
    schedule.setDescription("Test schedule");
    schedule.setModifiedDate(ZonedDateTime.now());
    return schedule;
  }

  private RequisitionGroupProgramSchedule generateRequisitionGroupProgramSchedule() {
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    Program program = generateProgram();
    Facility facility = generateFacility();
    requisitionGroupProgramSchedule.setProgram(program);
    requisitionGroupProgramSchedule.setDropOffFacility(facility);
    requisitionGroupProgramSchedule.setProcessingSchedule(schedule);
    return requisitionGroupProgramSchedule;
  }

  private Program generateProgram() {
    Program program = new Program("PROG" + generateInstanceNumber());
    program.setId(UUID.randomUUID());
    program.setName("name");
    return program;
  }

  private Facility generateFacility() {
    return new FacilityDataBuilder().build();
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
