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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.GeographicZoneSimpleDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;


import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"PMD.TooManyMethods"})
public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String BYLOCATION_URL = RESOURCE_URL + "/byLocation";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final Integer PAGE_NUMBER = 0;
  //Neither 0 nor Integer.MAX_VALUE work in this context
  private static final Integer PAGE_SIZE = 1000;
  private static final String LEVEL_NUMBER = "levelNumber";
  private static final String PARENT = "parent";

  private GeographicLevel countryLevel;
  private GeographicLevel regionLevel;

  private GeographicLevel districtLevel;
  private GeographicZone countryZone;
  private GeographicZone regionZone;
  private GeographicZone districtZone;
  
  private GeometryFactory gf;

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

    gf = new GeometryFactory();
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

    PageRequest pageRequest = new PageRequest(PAGE_NUMBER, PAGE_SIZE);
    given(geographicZoneRepository.findAll(pageRequest)).willReturn(geographicZonesPage);

    Page<GeographicZoneSimpleDto> response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(PAGE, PAGE_NUMBER)
        .queryParam(SIZE, PAGE_SIZE)
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

    given(geographicZoneService.search(any(Map.class), any(Pageable.class)))
        .willReturn(Pagination.getPage(geographicZones, null, geographicZones.size()));

    Map<String, Object> requestBody = getSearchBody();

    // when
    Page<GeographicZoneSimpleDto> response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    // then
    List<GeographicZoneSimpleDto> result = response.getContent();
    assertEquals(geographicZones.size(), result.size());
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

    Map<String, Object> requestBody = getSearchBody();

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnBadRequestOnException() {
    Map<String, Object> requestBody = getSearchBody();

    doThrow(new ValidationMessageException(
        GeographicZoneMessageKeys.ERROR_SEARCH_LACKS_PARAMS))
        .when(geographicZoneService)
        .search(any(Map.class), any(Pageable.class));

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(400);

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
  
  @Test
  public void findByLocationShouldFindGeographicZones() {
    // given
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    Point location = gf.createPoint(new Coordinate(3, 1));
    List<GeographicZone> geographicZones = Collections.singletonList(districtZone);
    given(geographicZoneRepository.findByLocation(location))
        .willReturn(geographicZones);

    // when
    GeographicZone[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(location)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(BYLOCATION_URL)
        .then()
        .statusCode(200)
        .extract().as(GeographicZone[].class);

    // then
    List<GeographicZone> result = Arrays.asList(response);
    assertEquals(geographicZones.size(), result.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
  
  @Test
  public void findByLocationShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    Point location = gf.createPoint(new Coordinate(3, 1));

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(location)
        .when()
        .post(BYLOCATION_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private Map<String, Object> getSearchBody() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(PAGE, PAGE_NUMBER);
    requestBody.put(SIZE, PAGE_SIZE);
    requestBody.put(LEVEL_NUMBER, districtLevel.getLevelNumber());
    requestBody.put(PARENT, regionZone.getId());
    return requestBody;
  }
}
