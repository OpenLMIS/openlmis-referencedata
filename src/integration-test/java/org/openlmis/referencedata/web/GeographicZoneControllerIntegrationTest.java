package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private GeographicZoneRepository geographicZoneRepository;

  private GeographicLevel geographicLevel;
  private GeographicZone geographicZone;
  private UUID geographicZoneId;

  /**
   * Constructor for tests.
   */
  public GeographicZoneControllerIntegrationTest() {
    geographicLevel = new GeographicLevel("GL1", 1);
    geographicZone = new GeographicZone("GZ1", geographicLevel);
    geographicZoneId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteGeographicZone() {

    given(geographicZoneRepository.findOne(geographicZoneId)).willReturn(geographicZone);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicZoneId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostGeographicZone() {

    GeographicZone response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(geographicZone)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(GeographicZone.class);

    assertEquals(geographicZone, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutGeographicZone() {

    geographicZone.setName("OpenLMIS");
    given(geographicZoneRepository.findOne(geographicZoneId)).willReturn(geographicZone);

    GeographicZone response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicZoneId)
        .body(geographicZone)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicZone.class);

    assertEquals(geographicZone, response);
    assertEquals("OpenLMIS", response.getName());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllGeographicZones() {

    List<GeographicZone> storedGeographicZones = Arrays.asList(geographicZone,
        new GeographicZone("GZ2", geographicLevel));
    given(geographicZoneRepository.findAll()).willReturn(storedGeographicZones);

    GeographicZone[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicZone[].class);

    assertEquals(storedGeographicZones.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetGeographicZone() {

    given(geographicZoneRepository.findOne(geographicZoneId)).willReturn(geographicZone);

    GeographicZone response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicZoneId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicZone.class);

    assertEquals(geographicZone, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
