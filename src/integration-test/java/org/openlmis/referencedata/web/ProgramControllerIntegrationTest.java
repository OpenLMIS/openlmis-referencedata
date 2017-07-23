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

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.dto.ProgramDto;
import org.springframework.http.MediaType;

public class ProgramControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/programs";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String FIND_BY_NAME_URL = RESOURCE_URL + "/search";

  private Program program;
  private ProgramDto programDto = new ProgramDto();
  private UUID programId;

  /**
   * Constructor for tests.
   */
  public ProgramControllerIntegrationTest() {
    program = new Program("code");
    program.setName("Program name");
    program.export(programDto);

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
        .body(programDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(Program.class);

    assertEquals(program, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenPostWithDuplicatedCode() {

    given(programRepository.findByCode(program.getCode())).willReturn(program);
    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(programDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutProgram() {

    programDto.setDescription("OpenLMIS");
    given(programRepository.findOne(programId)).willReturn(program);

    Program response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .body(programDto)
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
  public void shouldReturnBadRequestWhenPutWithDuplicatedCode() {

    program.setId(UUID.randomUUID());
    given(programRepository.findOne(programId)).willReturn(program);
    given(programRepository.findByCode(program.getCode())).willReturn(program);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .body(programDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

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

  @Test
  public void shouldFindProgramsByName() {
    String similarProgramName = "Program";
    List<Program> listToReturn = new ArrayList<>();
    listToReturn.add(program);
    given(programRepository.findProgramsByName(similarProgramName))
        .willReturn(listToReturn);
    Program[] response = restAssured.given()
        .queryParam("name", similarProgramName)
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(FIND_BY_NAME_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    List<Program> foundProgram = Arrays.asList(response);
    assertEquals(1, foundProgram.size());
    assertEquals("Program name", foundProgram.get(0).getName());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotFindProgramsByIncorrectName() {
    String incorrectProgramName = "Incorrect";
    given(programRepository.findProgramsByName(incorrectProgramName))
        .willReturn(new ArrayList<>());
    Program[] response = restAssured.given()
        .queryParam("name", incorrectProgramName)
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(FIND_BY_NAME_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    List<Program> foundProgram = Arrays.asList(response);
    assertEquals(0, foundProgram.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
