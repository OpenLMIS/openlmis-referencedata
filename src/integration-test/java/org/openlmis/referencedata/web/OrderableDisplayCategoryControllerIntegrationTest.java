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

import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OrderableDisplayCategoryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderableDisplayCategories";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String CODE = "code";

  @MockBean
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

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

    OrderableDisplayCategory[] response = restAssured
        .given()
        .queryParam(CODE, orderableDisplayCategory.getCode())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategory[].class);

    assertEquals(1, response.length);
    assertEquals(orderableDisplayCategory, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindAllOrderableDisplayCategories() {

    Iterable<OrderableDisplayCategory> searchResult =
        Collections.singletonList(orderableDisplayCategory);
    given(orderableDisplayCategoryRepository.findAll()).willReturn(searchResult);

    OrderableDisplayCategory[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategory[].class);

    assertEquals(1, response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private OrderableDisplayCategory generateOrderableDisplayCategory() {
    Integer instanceNumber = generateInstanceNumber();
    OrderableDisplayCategory orderableDisplayCategory = OrderableDisplayCategory.createNew(
        Code.code("orderableDisplayCategoryCode" + instanceNumber),
        new OrderedDisplayValue("orderableDisplayCategoryName" + instanceNumber, instanceNumber));
    return orderableDisplayCategory;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  @Test
  public void shouldDeleteOrderableDisplayCategory() {

    given(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId)).willReturn(orderableDisplayCategory);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostOrderableDisplayCategory() {

    OrderableDisplayCategory response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderableDisplayCategory)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(OrderableDisplayCategory.class);

    assertEquals(orderableDisplayCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutWithIdOrderableDisplayCategory() {

    given(orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId)).willReturn(orderableDisplayCategory);

    OrderableDisplayCategory response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .body(orderableDisplayCategory)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategory.class);

    assertEquals(orderableDisplayCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllOrderableDisplayCategories() {

    List<OrderableDisplayCategory> storedOrderableDisplayCategories = Arrays.asList(
        orderableDisplayCategory, generateOrderableDisplayCategory());
    given(orderableDisplayCategoryRepository.findAll()).willReturn(
        storedOrderableDisplayCategories);

    OrderableDisplayCategory[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategory[].class);

    assertEquals(storedOrderableDisplayCategories.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetOrderableDisplayCategory() {

    given(orderableDisplayCategoryRepository.findOne(orderableDisplayCategoryId)).willReturn(
        orderableDisplayCategory);

    OrderableDisplayCategory response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderableDisplayCategoryId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderableDisplayCategory.class);

    assertEquals(orderableDisplayCategory, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
