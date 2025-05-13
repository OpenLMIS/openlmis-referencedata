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

import static java.util.Arrays.asList;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.openlmis.referencedata.AvailableFeatures;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.togglz.junit.TogglzRule;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ProgramControllerIntegrationTest extends BaseWebIntegrationTest {

  @Rule
  public TogglzRule togglzRule = TogglzRule.builder(AvailableFeatures.class)
      .disable(AvailableFeatures.REDIS_CACHING)
      .build();

  private static final String RESOURCE_URL = "/api/programs";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String FIND_BY_NAME_URL = RESOURCE_URL + "/search";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "OpenLMIS";

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

    given(programRepository.findById(programId)).willReturn(Optional.of(program));

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
  public void shouldDeleteProgramFromCache() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    given(programRepository.findById(programId)).willReturn(Optional.of(program));
    given(programRedisRepository.exists(programId)).willReturn(true);
    given(programRedisRepository.findById(programId)).willReturn(program);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    verify(programRedisRepository, times(1)).delete(program);
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

    given(programRepository.save(program)).willThrow(new DataIntegrityViolationException("error"));
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

    programDto.setDescription(DESCRIPTION);
    given(programRepository.findById(programId)).willReturn(Optional.of(program));

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
  public void shouldDeleteProgramFromCacheAfterUpdate() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    mockUserHasRight(RightName.PROGRAMS_MANAGE);

    programDto.setDescription(DESCRIPTION);
    given(programRepository.findById(programId)).willReturn(Optional.of(program));
    given(programRedisRepository.findById(programId)).willReturn(program);
    given(programRedisRepository.exists(programId)).willReturn(true);

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
    verify(programRedisRepository, times(1)).delete(program);
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
    given(programRepository.save(program)).willThrow(new DataIntegrityViolationException("error"));

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

    List<Program> storedPrograms = asList(program, new Program("P2"));
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
  public void shouldFilterProgramsByIdsAndName() {
    String name = "some-name";
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    List<Program> storedPrograms = asList(program, new Program("P2"));
    given(programRepository.findByIdInAndNameIgnoreCaseContaining(asSet(id1, id2), name))
        .willReturn(storedPrograms);

    Program[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ID, id1)
        .queryParam(ID, id2)
        .queryParam(NAME, name)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertEquals(storedPrograms.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFilterProgramsByIds() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    List<Program> storedPrograms = asList(program, new Program("P2"));
    given(programRepository.findAllById(asSet(id1, id2))).willReturn(storedPrograms);

    Program[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(ID, id1)
        .queryParam(ID, id2)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertEquals(storedPrograms.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFilterProgramsByName() {
    String name = "some-name";

    List<Program> storedPrograms = asList(program, new Program("P2"));
    given(programRepository.findByNameIgnoreCaseContaining(name))
        .willReturn(storedPrograms);

    Program[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(NAME, name)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertEquals(storedPrograms.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProgramFromDatabaseWhenNotInCache() {

    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(programRepository.existsById(programId)).willReturn(true);
    given(programRedisRepository.exists(programId)).willReturn(false);
    given(programRepository.findById(programId)).willReturn(Optional.of(program));

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
    verify(programRedisRepository, times(1)).save(program);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProgram() {
    given(programRepository.existsById(programId)).willReturn(true);
    given(programRepository.findById(programId)).willReturn(Optional.of(program));

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
    verifyZeroInteractions(programRedisRepository);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProgramFromCache() {

    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(programRepository.existsById(programId)).willReturn(true);
    given(programRedisRepository.exists(programId)).willReturn(true);
    given(programRedisRepository.findById(programId)).willReturn(program);

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

    verify(programRedisRepository, times(1)).findById(programId);
    assertEquals(program, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldThrowErrorNotFoundWhenNeitherInDatabaseNorInCache() {
    togglzRule.enable(AvailableFeatures.REDIS_CACHING);
    given(supervisoryNodeRepository.existsById(programId)).willReturn(false);
    given(supervisoryNodeDtoRedisRepository.exists(programId)).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldThrowErrorNotFoundWhenNotInDatabase() {
    given(supervisoryNodeRepository.existsById(programId)).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", programId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    verifyZeroInteractions(programRedisRepository);
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

    List<Program> foundProgram = asList(response);
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

    List<Program> foundProgram = asList(response);
    assertEquals(0, foundProgram.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    given(programRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    given(programRepository.findById(any(UUID.class))).willReturn(Optional.of(program));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
