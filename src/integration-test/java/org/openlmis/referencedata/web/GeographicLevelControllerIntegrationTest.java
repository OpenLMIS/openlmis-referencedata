package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GeographicLevelControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicLevels";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";

  @MockBean
  private GeographicLevelRepository geographicLevelRepository;

  private GeographicLevel geographicLevel;
  private UUID geographicLevelId;

  public GeographicLevelControllerIntegrationTest() {
    geographicLevel = new GeographicLevel("GL1", 1);
    geographicLevelId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteGeographicLevel() {

    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicLevelId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostGeographicLevel() {

    GeographicLevel response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldPutGeographicLevel() {

    geographicLevel.setName("OpenLMIS");
    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    GeographicLevel response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldGetAllGeographicLevels() {

    List<GeographicLevel> storedGeographicLevels = Arrays.asList(geographicLevel,
        new GeographicLevel("GL2", 2));
    given(geographicLevelRepository.findAll()).willReturn(storedGeographicLevels);

    GeographicLevel[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldGetGeographicLevel() {

    given(geographicLevelRepository.findOne(geographicLevelId)).willReturn(geographicLevel);

    GeographicLevel response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
}
