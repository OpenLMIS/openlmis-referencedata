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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import guru.nidi.ramltester.junit.RamlMatchers;

public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private GeographicZoneRepository geographicZoneRepository;

  @MockBean
  private GeographicLevelRepository geographicLevelRepository;

  private GeographicLevel countryLevel;
  private GeographicLevel regionLevel;
  private GeographicLevel districtLevel;
  
  private GeographicZone countryZone;
  private GeographicZone regionZone;
  private GeographicZone districtZone;

  /**
   * Constructor for tests.
   */
  public GeographicZoneControllerIntegrationTest() {
    countryLevel = new GeographicLevel("Country", 1);
    countryLevel.setId(UUID.randomUUID());
    countryLevel.setName("Country");

    regionLevel = new GeographicLevel("Region", 1);
    regionLevel.setId(UUID.randomUUID());
    regionLevel.setName("Region");

    districtLevel = new GeographicLevel("District", 1);
    districtLevel.setId(UUID.randomUUID());
    districtLevel.setName("District");
    
    countryZone = new GeographicZone("TC", countryLevel);
    countryZone.setId(UUID.randomUUID());
    countryZone.setName("Test Country");
    countryZone.setCatchmentPopulation(100);
    countryZone.setLongitude(56.19);
    countryZone.setLatitude(33.15);

    regionZone = new GeographicZone("TR", regionLevel);
    regionZone.setId(UUID.randomUUID());
    regionZone.setName("Test Region");
    regionZone.setCatchmentPopulation(400);
    regionZone.setLongitude(41.76);
    regionZone.setLatitude(68.55);
    regionZone.setParent(countryZone);

    districtZone = new GeographicZone("TD", districtLevel);
    districtZone.setId(UUID.randomUUID());
    districtZone.setName("Test District");
    districtZone.setCatchmentPopulation(400);
    districtZone.setLongitude(41.76);
    districtZone.setLatitude(68.55);
    districtZone.setParent(regionZone);
  }

  @Test
  public void shouldDeleteGeographicZone() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);
    given(geographicZoneRepository.findOne(countryZone.getId())).willReturn(countryZone);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", countryZone.getId())
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
    when(geographicZoneRepository.save(countryZone)).thenReturn(countryZone);

    GeographicZone response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(countryZone)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(GeographicZone.class);

    assertEquals(countryZone, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutGeographicZone() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT, false);
    when(geographicZoneRepository.save(countryZone)).thenReturn(countryZone);

    GeographicZone response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", countryZone.getId())
        .body(countryZone)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicZone.class);

    assertEquals(countryZone, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllGeographicZones() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    List<GeographicZone> geographicZones = Arrays.asList(countryZone, regionZone, districtZone);
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
  public void shouldFindGeographicZonesByParentAndLevel() {
    // given
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    List<GeographicZone> geographicZones = Collections.singletonList(districtZone);
    Page<GeographicZone> geographicZonesPage = Pagination.getPage(geographicZones, null);
    PageRequest pageRequest = new PageRequest(0, 100);

    given(geographicZoneRepository.findOne(regionZone.getId())).willReturn(regionZone);
    given(geographicLevelRepository.findOne(districtLevel.getId())).willReturn(districtLevel);
    given(geographicZoneRepository.findByParentAndLevel(regionZone, districtLevel, pageRequest))
        .willReturn(geographicZonesPage);

    // when
    Page<GeographicZone> response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("page", pageRequest.getPageNumber())
        .queryParam("size", pageRequest.getPageSize())
        .queryParam("level", districtLevel.getId())
        .queryParam("parent", regionZone.getId())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    // then
    assertEquals(geographicZones.size(), response.getContent().size());
    assertEquals(geographicZones.size(), response.getNumberOfElements());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetGeographicZone() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    given(geographicZoneRepository.findOne(countryZone.getId())).willReturn(countryZone);

    GeographicZone response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", countryZone.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicZone.class);

    assertEquals(countryZone, response);
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
        .pathParam("id", countryZone.getId())
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
  public void searchShouldReturnForbiddenOnUnauthorizedToken() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("page", 0)
        .queryParam("size", 100)
        .queryParam("level", districtLevel.getId())
        .queryParam("parent", regionZone.getId())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(SEARCH_URL)
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
        .pathParam("id", countryZone.getId())
        .body(countryZone)
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
        .pathParam("id", countryZone.getId())
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
        .body(countryZone)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
