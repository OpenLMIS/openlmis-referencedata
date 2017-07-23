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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.CommodityType.newInstance;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys.ERROR_CLASSIFICATION_ID_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys.ERROR_CLASSIFICATION_SYSTEM_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys.ERROR_NAME_REQUIRED;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.CommodityTypeDto;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class CommodityTypeControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/commodityTypes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String TRADE_ITEMS_URL = ID_URL + "/tradeItems";
  private static final String CLASSIFICATION_SYS = "cSys";
  private static final String CLASSIFICATION_SYS_ID = "cSysId";
  private static final String NAME = "name";

  private CommodityTypeDto commodityType;
  private UUID commodityTypeId = UUID.randomUUID();

  @Before
  public void setUp() {
    commodityType = new CommodityTypeDto(NAME,
        CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID, null);

    when(commodityTypeRepository.save(any(CommodityType.class))).thenAnswer(new 
        SaveAnswer<CommodityType>());
    when(orderableRepository.save(any(Orderable.class))).thenAnswer(new SaveAnswer<Orderable>());
  }

  @Test
  public void shouldCreateNewCommodityType() {
    mockUserHasRight(ORDERABLES_MANAGE);

    CommodityTypeDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityTypeDto.class);

    assertEquals(commodityType, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateNewCommodityTypeIfNameIsEmpty() {
    mockUserHasRight(ORDERABLES_MANAGE);

    commodityType.setName("");
    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_NAME_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateNewCommodityTypeIfClassificationSystemIsEmpty() {
    mockUserHasRight(ORDERABLES_MANAGE);

    commodityType.setClassificationSystem("");
    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_CLASSIFICATION_SYSTEM_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateNewCommodityTypeIfClassificationIdIsEmpty() {
    mockUserHasRight(ORDERABLES_MANAGE);

    commodityType.setClassificationId("");
    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_CLASSIFICATION_ID_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateCommodityType() {
    mockUserHasRight(ORDERABLES_MANAGE);
    commodityType.setId(commodityTypeId);

    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));

    CommodityTypeDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityTypeDto.class);

    assertEquals(commodityType, response);
    assertEquals(commodityType.getId(), response.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutCommodityTypeIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveCommodityTypes() {
    mockUserHasRight(ORDERABLES_MANAGE);

    CommodityTypeDto commodityType2 = new CommodityTypeDto("name2", "csys2", "csysid2", null);

    List<CommodityType> commodityTypes = Arrays.asList(
        CommodityType.newInstance(commodityType),
        CommodityType.newInstance(commodityType2));

    when(commodityTypeRepository.findAll()).thenReturn(commodityTypes);

    PageImplRepresentation response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<CommodityTypeDto> expected = CommodityTypeDto.newInstance(commodityTypes);

    checkIfEquals(response, expected);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateTradeItemAssociations() {
    mockUserHasRight(ORDERABLES_MANAGE);
    commodityType.setId(commodityTypeId);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));

    TradeItem tradeItem = mockTradeItem();
    TradeItem anotherTradeItem = mockTradeItem();
    List<UUID> tradeItemIds = asList(tradeItem.getId(), anotherTradeItem.getId());

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", commodityTypeId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(tradeItemIds)
        .when()
        .put(TRADE_ITEMS_URL)
        .then()
        .statusCode(200);

    ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
    verify(tradeItemRepository).save(captor.capture());

    Set<TradeItem> itemsToUpdate = captor.getValue();
    assertThat(itemsToUpdate, hasSize(2));
    assertThat(itemsToUpdate, hasItems(tradeItem, anotherTradeItem));
  }

  @Test
  public void shouldRejectUpdateTradeItemAssociationsIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);
    commodityType.setId(commodityTypeId);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));

    TradeItem tradeItem = mockTradeItem();
    TradeItem anotherTradeItem = mockTradeItem();
    List<UUID> tradeItemIds = asList(tradeItem.getId(), anotherTradeItem.getId());

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", commodityTypeId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(tradeItemIds)
        .when()
        .put(TRADE_ITEMS_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetTradeItemAssociations() {
    mockUserHasRight(ORDERABLES_MANAGE);
    commodityType.setId(commodityTypeId);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));

    TradeItem tradeItem = mockTradeItem();
    TradeItem anotherTradeItem = mockTradeItem();

    given(tradeItemRepository.findByClassificationId(CLASSIFICATION_SYS_ID))
        .willReturn(asList(tradeItem, anotherTradeItem));

    UUID[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", commodityTypeId)
        .when()
        .get(TRADE_ITEMS_URL)
        .then()
        .statusCode(200)
        .extract().as(UUID[].class);
    List<UUID> uuids = asList(response);

    assertEquals(2, uuids.size());
    assertTrue(uuids.contains(tradeItem.getId()));
    assertTrue(uuids.contains(anotherTradeItem.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetTradeItemAssociationsIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);
    commodityType.setId(commodityTypeId);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", commodityTypeId)
        .when()
        .get(TRADE_ITEMS_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateCommodityTypeWithParent() {
    mockUserHasRight(ORDERABLES_MANAGE);

    UUID parentId = UUID.randomUUID();
    CommodityTypeDto parent = generateParent(parentId);
    commodityType = new CommodityTypeDto(NAME, CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID, parent);

    commodityType.setId(commodityTypeId);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));
    given(commodityTypeRepository.findOne(parentId))
        .willReturn(newInstance(parent));

    CommodityTypeDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityTypeDto.class);

    assertEquals(commodityType, response);
    assertEquals(commodityType.getParent().getId(), response.getParent().getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfParentNotFound() {
    mockUserHasRight(ORDERABLES_MANAGE);

    UUID parentId = UUID.randomUUID();
    CommodityTypeDto parent = generateParent(parentId);

    commodityType = new CommodityTypeDto(NAME,  CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID, parent);
    commodityType.setId(commodityTypeId);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(newInstance(commodityType));

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .content(LocalizedMessage.MESSAGE_KEY_FIELD,
            is(CommodityTypeMessageKeys.ERROR_PARENT_NOT_FOUND));
  }

  private CommodityTypeDto generateParent(UUID parentId) {
    CommodityTypeDto parent = new CommodityTypeDto("parentProd", CLASSIFICATION_SYS,
        CLASSIFICATION_SYS_ID, null);
    parent.setId(parentId);
    return parent;
  }

  private TradeItem mockTradeItem() {
    UUID id = UUID.randomUUID();
    TradeItem tradeItem = new TradeItem("manufacturer", new ArrayList<>());
    tradeItem.setId(id);
    when(tradeItemRepository.findOne(id)).thenReturn(tradeItem);
    return tradeItem;
  }

  private void checkIfEquals(PageImplRepresentation response, List<CommodityTypeDto> expected) {
    List pageContent = response.getContent();
    assertEquals(expected.size(), pageContent.size());
    for (int i = 0; i < pageContent.size(); i++) {
      Map<String, String> retrieved = (LinkedHashMap) pageContent.get(i);
      assertEquals(expected.get(i).getName(),
          retrieved.get("name"));
      assertEquals(expected.get(i).getClassificationSystem(),
          retrieved.get("classificationSystem"));
      assertEquals(expected.get(i).getClassificationId(),
          retrieved.get("classificationId"));
    }
  }

}
