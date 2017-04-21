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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import org.junit.Test;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.List;

public class TradeItemControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/tradeItems";
  private static final String CID = "cid";

  @MockBean
  private TradeItemRepository repository;


  @Test
  public void shouldCreateNewTradeItem() {
    mockUserHasRight(ORDERABLES_MANAGE);

    TradeItem tradeItem = generateItem("item");

    when(repository.save(tradeItem)).thenReturn(tradeItem);

    TradeItem response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(tradeItem)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(TradeItem.class);

    assertEquals(tradeItem, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAllTradeItems() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<TradeItem> items = asList(generateItem("one"),
        generateItem("two"));

    when(repository.findAll()).thenReturn(items);

    TradeItem[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(TradeItem[].class);

    assertArrayEquals(items.toArray(), response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveTradeItemsByPartialMatch() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<TradeItem> items = asList(generateItem("one"),
        generateItem("two"));

    when(repository.findByClassificationIdLike(CID)).thenReturn(items);

    TradeItem[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("classificationId", CID)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(TradeItem[].class);

    assertArrayEquals(items.toArray(), response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveTradeItemsByFullMatch() {
    mockUserHasRight(ORDERABLES_MANAGE);
    List<TradeItem> items = asList(generateItem("one"),
        generateItem("two"));

    when(repository.findByClassificationId(CID)).thenReturn(items);

    TradeItem[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("classificationId", CID)
        .queryParam("fullMatch", "true")
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(TradeItem[].class);

    assertArrayEquals(items.toArray(), response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDenyAccessToUpdate() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .body(generateItem("code"))
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);
  }

  @Test
  public void shouldDenyAccessToRetrieve() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);
  }

  private TradeItem generateItem(String productCode) {
    TradeItem tradeItem = TradeItem.newTradeItem(productCode, "each", productCode, 10, 20, false);
    tradeItem.assignCommodityType("sys1", "sys1Id");
    tradeItem.assignCommodityType("sys2", "sys2Id");
    return tradeItem;
  }
}
