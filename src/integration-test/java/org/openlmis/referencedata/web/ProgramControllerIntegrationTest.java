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

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
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
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    given(programRepository.findOne(programId)).willReturn(program);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteProgramIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROGRAMS_MANAGE);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(MESSAGEKEY_ERROR_UNAUTHORIZED));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostProgram() {
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    Program response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldRejectPostProgramIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROGRAMS_MANAGE);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(programDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(MESSAGEKEY_ERROR_UNAUTHORIZED));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenPostWithDuplicatedCode() {
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    given(programRepository.findByCode(program.getCode())).willReturn(program);
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    programDto.setDescription("OpenLMIS");
    given(programRepository.findOne(programId)).willReturn(program);

    Program response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldRejectPutProgramIfUserHasNoRight() {
    mockUserHasNoRight(RightName.PROGRAMS_MANAGE);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .body(programDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(MESSAGEKEY_ERROR_UNAUTHORIZED));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenPutWithDuplicatedCode() {
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    program.setId(UUID.randomUUID());
    given(programRepository.findOne(programId)).willReturn(program);
    given(programRepository.findByCode(program.getCode())).willReturn(program);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    given(programRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    given(programRepository.findOne(any(UUID.class))).willReturn(program);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
