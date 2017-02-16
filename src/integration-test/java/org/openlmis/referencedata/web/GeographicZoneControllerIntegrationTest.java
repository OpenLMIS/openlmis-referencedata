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

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

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
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);
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
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);
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
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);
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
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    List<GeographicZone> geographicZones = Arrays.asList(geographicZone, geographicZone2);
    Page<GeographicZone> geographicZonesPage = Pagination.getPage(geographicZones, null);

    int pageNumber = 0;
    int pageSize = 1000; //Neither 0 nor Integer.MAX_VALUE work in this context

    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    given(geographicZoneRepository.findAll(pageRequest)).willReturn(geographicZonesPage);

    Page<GeographicZone> response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("page", pageNumber)
        .queryParam("size", pageSize)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(geographicZones.size(), response.getContent().size());
    assertEquals(geographicZones.size(), response.getNumberOfElements());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetGeographicZone() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

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
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

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
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

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
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

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
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

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
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);

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
