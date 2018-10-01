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

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.GeographicZoneDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class GeographicZoneControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/geographicZones";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String BY_LOCATION_URL = RESOURCE_URL + "/byLocation";

  private static final String LEVEL_NUMBER = "levelNumber";
  private static final String PARENT = "parent";

  private GeographicZone countryZone;
  private GeographicZone regionZone;
  private GeographicZone districtZone;

  private GeographicZoneDto countryZoneDto = new GeographicZoneDto();
  private GeographicZoneDto regionZoneDto = new GeographicZoneDto();
  private GeographicZoneDto districtZoneDto = new GeographicZoneDto();

  private GeometryFactory gf;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    countryZone = new GeographicZoneDataBuilder()
        .withName("Country")
        .withLevel(
            new GeographicLevelDataBuilder()
                .withName("Country")
                .withLevelNumber(1)
                .build()
        )
        .build();

    regionZone = new GeographicZoneDataBuilder()
        .withName("Region")
        .withParent(countryZone)
        .withLevel(
            new GeographicLevelDataBuilder()
                .withName("Region")
                .withLevelNumber(2)
                .build()
        )
        .build();

    districtZone = new GeographicZoneDataBuilder()
        .withName("District")
        .withParent(regionZone)
        .withLevel(
            new GeographicLevelDataBuilder()
                .withName("District")
                .withLevelNumber(3)
                .build()
        )
        .build();

    gf = new GeometryFactory();

    countryZone.export(countryZoneDto);
    regionZone.export(regionZoneDto);
    districtZone.export(districtZoneDto);

    given(geographicZoneRepository.save(any(GeographicZone.class))).willAnswer(new SaveAnswer<>());
    given(geographicZoneRepository.findOne(countryZoneDto.getId())).willReturn(countryZone);
    given(geographicZoneRepository.findOne(regionZoneDto.getId())).willReturn(regionZone);
    given(geographicZoneRepository.findOne(districtZoneDto.getId())).willReturn(districtZone);

    mockUserHasRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);
  }

  @Test
  public void shouldDeleteGeographicZone() {
    given(geographicZoneRepository.exists(countryZoneDto.getId())).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, countryZoneDto.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostGeographicZone() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(countryZoneDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(hasSameFields(countryZoneDto, ID));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutGeographicZone() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, countryZoneDto.getId())
        .body(countryZoneDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(hasSameFields(countryZoneDto));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllGeographicZones() {
    List<GeographicZone> geographicZones = Lists
        .newArrayList(countryZone, regionZone, districtZone);
    Page<GeographicZone> geographicZonesPage = Pagination.getPage(geographicZones, null);

    given(geographicZoneRepository.findAll(pageable)).willReturn(geographicZonesPage);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(CONTENT, hasSize(geographicZones.size()))
        .body(CONTENT_ID, hasSize(geographicZones.size()))
        .body(CONTENT_ID, hasItems(countryZone.getId().toString(), regionZone.getId().toString(),
            districtZone.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindGeographicZonesByParentAndLevel() {
    // given
    List<GeographicZone> geographicZones = Collections.singletonList(districtZone);

    given(geographicZoneService.search(anyMapOf(String.class, Object.class), any(Pageable.class)))
        .willReturn(Pagination.getPage(geographicZones, null, geographicZones.size()));

    Map<String, Object> requestBody = getSearchBody();

    // when
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(CONTENT, hasSize(geographicZones.size()))
        .body(CONTENT_ID, hasSize(geographicZones.size()))
        .body(CONTENT_ID, hasItems(districtZone.getId().toString()));

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetGeographicZone() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, countryZoneDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(hasSameFields(countryZoneDto));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnUnauthorizedWithoutAuthorization() {
    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, countryZoneDto.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldReturnUnauthorizedWithoutAuthorization() {
    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {
    Map<String, Object> requestBody = getSearchBody();

    restAssured
        .given()
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnBadRequestOnException() {
    Map<String, Object> requestBody = getSearchBody();

    doThrow(new ValidationMessageException(
        GeographicZoneMessageKeys.ERROR_SEARCH_LACKS_PARAMS))
        .when(geographicZoneService)
        .search(anyMapOf(String.class, Object.class), any(Pageable.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(requestBody)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnForbiddenOnUnauthorizedToken() {
    mockUserHasNoRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, countryZoneDto.getId())
        .body(countryZoneDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnForbiddenOnUnauthorizedToken() {
    mockUserHasNoRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, countryZoneDto.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void postShouldReturnForbiddenOnUnauthorizedToken() {
    mockUserHasNoRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(countryZoneDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findByLocationShouldFindGeographicZones() {
    // given
    Point location = gf.createPoint(new Coordinate(3, 1));
    List<GeographicZone> geographicZones = Collections.singletonList(districtZone);
    given(geographicZoneRepository.findByLocation(location))
        .willReturn(geographicZones);

    // when
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .body(location)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(BY_LOCATION_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("", hasSize(geographicZones.size()))
        .body(ID, hasSize(geographicZones.size()))
        .body(ID, hasItems(districtZone.getId().toString()));

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findByLocationShouldReturnForbiddenForUnauthorizedToken() {
    mockUserHasNoRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    Point location = gf.createPoint(new Coordinate(3, 1));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(location)
        .when()
        .post(BY_LOCATION_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    given(geographicZoneRepository.exists(any(UUID.class))).willReturn(false);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    mockUserHasNoRight(RightName.GEOGRAPHIC_ZONES_MANAGE_RIGHT);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    given(geographicZoneRepository.exists(any(UUID.class))).willReturn(true);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  private Map<String, Object> getSearchBody() {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put(PAGE, pageable.getPageNumber());
    requestBody.put(SIZE, pageable.getPageSize());
    requestBody.put(LEVEL_NUMBER, districtZoneDto.getLevel().getLevelNumber());
    requestBody.put(PARENT, regionZoneDto.getId());
    return requestBody;
  }

}
