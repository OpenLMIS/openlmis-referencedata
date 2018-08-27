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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.OrderableDisplayCategoryDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderableDisplayCategoryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderableDisplayCategories";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String CODE = "code";

  private Integer currentInstanceNumber;

  private OrderableDisplayCategory orderableDisplayCategory;
  private UUID orderableDisplayCategoryId;

  /**
   * Constructor for tests.
   */
  public OrderableDisplayCategoryControllerIntegrationTest() {
    currentInstanceNumber = 0;
    orderableDisplayCategory = generateOrderableDisplayCategory();
    orderableDisplayCategoryId = UUID.randomUUID();
  }

  @Test
  public void shouldFindOrderableDisplayCategoriesByCode() {

    given(orderableDisplayCategoryRepository.findByCode(any(Code.class))).willReturn(
        orderableDisplayCategory);

    OrderableDisplayCategoryDto[] response = restAssured
        .given()
        .queryParam(CODE, orderableDisplayCategory.getCode())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategoryDto[].class);

    assertEquals(1, response.length);
    assertEquals(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory), response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindAllOrderableDisplayCategories() {

    Iterable<OrderableDisplayCategory> searchResult =
        Collections.singletonList(orderableDisplayCategory);
    given(orderableDisplayCategoryRepository.findAll()).willReturn(searchResult);

    OrderableDisplayCategoryDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategoryDto[].class);

    assertEquals(1, response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteOrderableDisplayCategory() {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId)).willReturn(orderableDisplayCategory);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteOrderableDisplayCategoryIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    given(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId)).willReturn(orderableDisplayCategory);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
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
  public void shouldPostOrderableDisplayCategory() {
    mockUserHasRight(ORDERABLES_MANAGE);

    OrderableDisplayCategoryDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory))
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(OrderableDisplayCategoryDto.class);

    assertEquals(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory), response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostOrderableDisplayCategoryIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory))
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
  public void shouldPutWithIdOrderableDisplayCategory() {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId)).willReturn(orderableDisplayCategory);

    OrderableDisplayCategoryDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .body(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategoryDto.class);

    assertEquals(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory), response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutOrderableDisplayCategoryIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    given(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId)).willReturn(orderableDisplayCategory);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .body(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory))
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
  public void shouldGetAllOrderableDisplayCategories() {

    List<OrderableDisplayCategory> storedOrderableDisplayCategories = Arrays.asList(
        orderableDisplayCategory, generateOrderableDisplayCategory());
    given(orderableDisplayCategoryRepository.findAll()).willReturn(
        storedOrderableDisplayCategories);

    OrderableDisplayCategoryDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategoryDto[].class);

    assertEquals(storedOrderableDisplayCategories.size(), response.length);
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
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetOrderableDisplayCategory() {

    given(orderableDisplayCategoryRepository.findOne(orderableDisplayCategoryId)).willReturn(
        orderableDisplayCategory);

    OrderableDisplayCategoryDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategoryDto.class);

    assertEquals(OrderableDisplayCategoryDto.newInstance(orderableDisplayCategory), response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnUnauthorizedWithoutAuthorization() {

    restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(401);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.ORDERABLES_MANAGE);
    given(orderableDisplayCategoryRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.ORDERABLES_MANAGE);
    given(orderableDisplayCategoryRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.ORDERABLES_MANAGE);
    given(orderableDisplayCategoryRepository.findOne(any(UUID.class)))
        .willReturn(orderableDisplayCategory);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private OrderableDisplayCategory generateOrderableDisplayCategory() {
    Integer instanceNumber = generateInstanceNumber();
    return OrderableDisplayCategory.createNew(
        Code.code("orderableDisplayCategoryCode" + instanceNumber),
        new OrderedDisplayValue("orderableDisplayCategoryName" + instanceNumber, instanceNumber));
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
