package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String GEOGRAPHIC_ZONE_MANAGE_RIGHT = "GEOGRAPHIC_ZONE_MANAGE";

  @MockBean
  private GeographicZoneRepository geographicZoneRepository;

  private GeographicLevel geographicLevel;
  private GeographicZone geographicZone;
  private GeographicZone geographicZone2;
  private UUID geographicZoneId;

  /**
   * Constructor for tests.
   */
  public GeographicZoneControllerIntegrationTest() {
    geographicLevel = new GeographicLevel("GL1", 1);
    geographicLevel.setId(UUID.randomUUID());
    geographicLevel.setName("district");
    geographicZoneId = UUID.randomUUID();

    geographicZone = new GeographicZone("GZ1", geographicLevel);
    geographicZone.setId(geographicZoneId);
    geographicZone.setName("1");
    geographicZone.setCatchmentPopulation(100);
    geographicZone.setLongitude(56.19);
    geographicZone.setLatitude(33.15);

    geographicZone2 = new GeographicZone("GZ2", geographicLevel);
    geographicZone2.setId(UUID.randomUUID());
    geographicZone2.setName("2");
    geographicZone2.setCatchmentPopulation(400);
    geographicZone2.setLongitude(41.76);
    geographicZone2.setLatitude(68.55);
  }

  @Test
  public void shouldDeleteGeographicZone() {
    doNothing()
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT, false);
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
    doNothing()
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT, false);
    when(geographicZoneRepository.save(geographicZone)).thenReturn(geographicZone);

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
    doNothing()
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT, false);
    when(geographicZoneRepository.save(geographicZone)).thenReturn(geographicZone);

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
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllGeographicZones() {
    doNothing()
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT);

    List<GeographicZone> storedGeographicZones = Arrays.asList(geographicZone, geographicZone2);
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
    doNothing()
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT);

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

  @Test
  public void getShouldReturnForbiddenOnUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, GEOGRAPHIC_ZONE_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicZoneId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldReturnForbiddenOnUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, GEOGRAPHIC_ZONE_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnForbiddenOnUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, GEOGRAPHIC_ZONE_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT, false);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicZoneId)
        .body(geographicZone)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnForbiddenOnUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, GEOGRAPHIC_ZONE_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT, false);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", geographicZoneId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postShouldReturnForbiddenOnUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, GEOGRAPHIC_ZONE_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(GEOGRAPHIC_ZONE_MANAGE_RIGHT, false);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(geographicZone)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
