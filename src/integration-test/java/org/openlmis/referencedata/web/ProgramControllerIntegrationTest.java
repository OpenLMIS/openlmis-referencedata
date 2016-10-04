package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProgramControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/programs";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private ProgramRepository programRepository;

  private Program program;
  private UUID programId;

  public ProgramControllerIntegrationTest() {
    program = new Program("code");
    programId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteProgram() {

    given(programRepository.findOne(programId)).willReturn(program);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostProgram() {

    Program response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(program)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(Program.class);

    assertEquals(program, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutProgram() {

    program.setDescription("OpenLMIS");
    given(programRepository.findOne(programId)).willReturn(program);

    Program response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .body(program)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(Program.class);

    assertEquals(program, response);
    assertEquals("OpenLMIS", response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllPrograms() {

    List<Program> storedPrograms = Arrays.asList(program, new Program("P2"));
    given(programRepository.findAll()).willReturn(storedPrograms);

    Program[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertEquals(storedPrograms.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProgram() {

    given(programRepository.findOne(programId)).willReturn(program);

    Program response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(Program.class);

    assertEquals(program, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
