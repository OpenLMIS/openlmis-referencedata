package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProcessingScheduleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingSchedules";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ACCESS_TOKEN = "access_token";

  @MockBean
  private ProcessingScheduleRepository scheduleRepository;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

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
    schedule = new ProcessingSchedule("PS1", "Schedule1");
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

    given(scheduleRepository.findOne(processingScheduleId)).willReturn(schedule);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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

    ProcessingSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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

    schedule.setDescription("OpenLMIS");
    given(scheduleRepository.findOne(processingScheduleId)).willReturn(schedule);

    ProcessingSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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

    List<ProcessingSchedule> storedProcessingSchedules = Arrays.asList(schedule,
        new ProcessingSchedule("PS2", "Schedule2"));
    given(scheduleRepository.findAll()).willReturn(storedProcessingSchedules);

    ProcessingSchedule[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingSchedule[].class);

    assertEquals(storedProcessingSchedules.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingSchedule() {

    given(scheduleRepository.findOne(processingScheduleId)).willReturn(schedule);

    ProcessingSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(requisitionGroupProgramSchedule);

    ProcessingSchedule[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(null);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(requisitionGroupProgramSchedule);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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

    given(facilityRepository.findOne(facilityId)).willReturn(null);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(requisitionGroupProgramSchedule);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
