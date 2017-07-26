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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.openlmis.referencedata.domain.RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GeographicLevelControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicLevels";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private GeographicLevel geographicLevel;
  private UUID geographicLevelId;

  public GeographicLevelControllerIntegrationTest() {
    geographicLevel = new GeographicLevel("GL1", 1);
    geographicLevelId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteGeographicLevel() {
    mockUserHasRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteGeographicLevelIfUserHasNoRight() {
    mockUserHasNoRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostGeographicLevel() {
    mockUserHasRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    GeographicLevel response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(geographicLevel)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(GeographicLevel.class);

    assertEquals(geographicLevel, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostGeographicLevelIfUserHasNoRight() {
    mockUserHasNoRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(geographicLevel)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutGeographicLevel() {
    mockUserHasRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    geographicLevel.setName("OpenLMIS");
    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    GeographicLevel response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .body(geographicLevel)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicLevel.class);

    assertEquals(geographicLevel, response);
    assertEquals("OpenLMIS", response.getName());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutGeographicLevelIfUserHasNoRight() {
    mockUserHasNoRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    geographicLevel.setName("OpenLMIS");
    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .body(geographicLevel)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllGeographicLevels() {
    mockUserHasRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    List<GeographicLevel> storedGeographicLevels = Arrays.asList(geographicLevel,
        new GeographicLevel("GL2", 2));
    given(geographicLevelRepository.findAll()).willReturn(storedGeographicLevels);

    GeographicLevel[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicLevel[].class);

    assertEquals(storedGeographicLevels.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetAllGeographicLevelsIfUserHasNoRight() {
    mockUserHasNoRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    List<GeographicLevel> storedGeographicLevels = Arrays.asList(geographicLevel,
        new GeographicLevel("GL2", 2));
    given(geographicLevelRepository.findAll()).willReturn(storedGeographicLevels);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetGeographicLevel() {
    mockUserHasRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    GeographicLevel response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicLevel.class);

    assertEquals(geographicLevel, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetGeographicLevelIfUserHasNoRight() {
    mockUserHasNoRight(GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
