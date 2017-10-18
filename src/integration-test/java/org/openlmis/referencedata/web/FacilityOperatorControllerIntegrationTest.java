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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityOperatorControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/facilityOperators";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private FacilityOperator facilityOperator;
  private UUID facilityOperatorId;

  private Integer currentInstanceNumber = 0;

  @Before
  public void setUp() {
    currentInstanceNumber = generateInstanceNumber();
    facilityOperator = generateFacilityOperator();
  }

  @Test
  public void shouldPostFacilityOperator() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    FacilityOperator response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityOperator)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(FacilityOperator.class);

    assertEquals(facilityOperator, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateRequestIfUserHasNoRight() {
    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityOperator)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllFacilityOperators() {

    List<FacilityOperator> facilityOperators = Arrays.asList(facilityOperator,
        generateFacilityOperator());

    given(facilityOperatorRepository.findAll()).willReturn(facilityOperators);

    FacilityOperator[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityOperator[].class);

    assertEquals(2, response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutFacilityOperator() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);

    facilityOperator.setName("NewNameUpdate");

    given(facilityOperatorRepository.findOne(facilityOperatorId)).willReturn(facilityOperator);

    FacilityOperator response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .body(facilityOperator)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityOperator.class);

    assertEquals(facilityOperator, response);
    assertEquals("NewNameUpdate", response.getName());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectUpdateRequestIfUserHasNoRight() {
    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .body(facilityOperator)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
  
  @Test
  public void shouldGetFacilityOperator() {

    given(facilityOperatorRepository.findOne(facilityOperatorId)).willReturn(facilityOperator);

    FacilityOperator response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityOperator.class);

    assertEquals(facilityOperator, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteFacilityOperator() {
    mockUserHasRight(RightName.FACILITIES_MANAGE_RIGHT);
    
    given(facilityOperatorRepository.findOne(facilityOperatorId)).willReturn(facilityOperator);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteRequestIfUserHasNoRight() {
    mockUserHasNoRight(RightName.FACILITIES_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityOperatorRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityOperatorRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    given(facilityOperatorRepository.findOne(any(UUID.class))).willReturn(facilityOperator);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private FacilityOperator generateFacilityOperator() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperatorId = UUID.randomUUID();
    facilityOperator.setId(facilityOperatorId);
    facilityOperator.setName("FacilityOperatorName" + currentInstanceNumber);
    facilityOperator.setCode("code" + currentInstanceNumber);
    facilityOperator.setDescription("description" + currentInstanceNumber);
    facilityOperator.setDisplayOrder(currentInstanceNumber);
    return facilityOperator;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber++;
    return currentInstanceNumber;
  }
}
