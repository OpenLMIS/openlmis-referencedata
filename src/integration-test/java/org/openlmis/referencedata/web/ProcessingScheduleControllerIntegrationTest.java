package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProcessingScheduleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingSchedules";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";

  @MockBean
  private ProcessingScheduleRepository scheduleRepository;

  private ProcessingSchedule schedule;
  private UUID processingScheduleId;

  public ProcessingScheduleControllerIntegrationTest() {
    schedule = new ProcessingSchedule("PS1", "Schedule1");
    processingScheduleId = UUID.randomUUID();
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
}
