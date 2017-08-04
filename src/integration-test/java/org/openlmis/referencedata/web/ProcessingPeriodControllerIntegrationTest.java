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
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;

import com.google.common.collect.Sets;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.dto.ResultDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;


@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingPeriodControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingPeriods";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String SEARCH_BY_UUID_AND_DATE_URL =
      RESOURCE_URL + "/searchByScheduleAndDate";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DIFFERENCE_URL = RESOURCE_URL + "/{id}/duration";
  private static final String PROGRAM = "programId";
  private static final String FACILITY = "facilityId";
  private static final String PROCESSING_SCHEDULE = "processingScheduleId";
  private static final String START_DATE = "startDate";

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
    given(periodRepository.findOne(secondPeriod.getId())).willReturn(secondPeriod);

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
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

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
  public void shouldFindPeriodsByProgramAndFacility() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(programRepository.findOne(programId))
        .willReturn(requisitionGroupProgramSchedule.getProgram());
    given(facilityRepository.findOne(facilityId))
        .willReturn(requisitionGroupProgramSchedule.getDropOffFacility());

    given(periodService.filterPeriods(requisitionGroupProgramSchedule.getProgram(),
        requisitionGroupProgramSchedule.getDropOffFacility()))
        .willReturn(Arrays.asList(firstPeriod, secondPeriod));

    ProcessingPeriodDto[] response = restAssured.given()
        .queryParam(PROGRAM, programId)
        .queryParam(FACILITY, facilityId)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(2, response.length);
  }

  @Test
  public void shouldFindPeriodsByScheduleAndDate() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(scheduleRepository.findOne(scheduleId)).willReturn(schedule);
    given(periodService.searchPeriods(schedule, secondPeriod.getStartDate()))
        .willReturn(Arrays.asList(secondPeriod, firstPeriod));

    ProcessingPeriodDto[] response = restAssured.given()
        .queryParam(PROCESSING_SCHEDULE, scheduleId)
        .queryParam(START_DATE, secondPeriod.getStartDate().toString())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_BY_UUID_AND_DATE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(2, response.length);
    for (ProcessingPeriodDto period : response) {
      assertEquals(
          period.getProcessingSchedule().getId(),
          firstPeriod.getProcessingSchedule().getId());
    }
  }

  @Test
  public void shouldDeletePeriod() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutPeriod() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    firstPeriod.setDescription("OpenLMIS");
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);

    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

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
  public void shouldGetAllPeriods() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    Set<ProcessingPeriod> storedPeriods = Sets.newHashSet(firstPeriod, secondPeriod, thirdPeriod);
    given(periodRepository.findAll()).willReturn(storedPeriods);

    ProcessingPeriodDto[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto[].class);

    List<ProcessingPeriodDto> periods = Arrays.asList(response);
    assertEquals(3, periods.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllPeriodsSortedByStartDate() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    Set<ProcessingPeriod> storedPeriods = Sets.newHashSet(firstPeriod, secondPeriod, thirdPeriod);
    given(periodRepository.findAll()).willReturn(storedPeriods);

    ProcessingPeriodDto[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("sort", "startDate")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto[].class);

    List<ProcessingPeriodDto> periods = Arrays.asList(response);
    assertEquals(3, periods.size());
    assertEquals(firstPeriod.getStartDate(), periods.get(0).getStartDate());
    assertEquals(secondPeriod.getStartDate(), periods.get(1).getStartDate());
    assertEquals(thirdPeriod.getStartDate(), periods.get(2).getStartDate());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRespondWithBadRequestWhenInvalidSortProperty() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    Set<ProcessingPeriod> storedPeriods = Sets.newHashSet(firstPeriod, secondPeriod, thirdPeriod);
    given(periodRepository.findAll()).willReturn(storedPeriods);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("sort", "invalid")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenPeriod() {
    mockUserHasRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);
    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

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
  public void shouldReturnUnauthorizedOnDisplayTotalDurationIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured.given()
        .pathParam("id", firstPeriodId)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(DIFFERENCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnDeletePeriodIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

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
  public void shouldReturnUnauthorizedOnGetAllPeriodsIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedOnGetChosenPeriodIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROCESSING_SCHEDULES_MANAGE_RIGHT);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", firstPeriodId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProcessingSchedule generateSchedule() {
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setId(UUID.randomUUID());
    schedule.setCode("S" + generateInstanceNumber());
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
    program.setName("name");
    return program;
  }

  private Facility generateFacility() {
    Facility facility = new Facility("F" + generateInstanceNumber());
    FacilityType facilityType = generateFacilityType();
    GeographicZone geographicZone = generateGeographicZone();

    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("facilityName");
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FT" + generateInstanceNumber());
    return facilityType;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel level = new GeographicLevel();
    level.setCode("GL" + generateInstanceNumber());
    level.setLevelNumber(1);
    return level;
  }

  private GeographicZone generateGeographicZone() {
    GeographicZone geographicZone = new GeographicZone();
    GeographicLevel level = generateGeographicLevel();
    geographicZone.setLevel(level);
    geographicZone.setCode("GZ" + generateInstanceNumber());
    return geographicZone;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
