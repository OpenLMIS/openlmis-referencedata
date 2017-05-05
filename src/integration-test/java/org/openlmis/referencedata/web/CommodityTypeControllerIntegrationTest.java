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
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.OrderedDisplayValue;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.LocalizedMessage;
import org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class CommodityTypeControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/commodityTypes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String TRADE_ITEMS_URL = ID_URL + "/tradeItems";
  private static final String UNIT = "unit";
  private static final String CLASSIFICATION_SYS = "cSys";
  private static final String CLASSIFICATION_SYS_ID = "cSysId";

  @MockBean
  private CommodityTypeRepository commodityTypeRepository;

  @MockBean
  private OrderableRepository orderableRepository;

  @MockBean
  private TradeItemRepository tradeItemRepository;

  @MockBean
  private ProgramRepository programReposiroty;

  @MockBean
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  private CommodityType commodityType;
  private UUID commodityTypeId;

  @Before
  public void setUp() {
    commodityType = CommodityType.newCommodityType("code", UNIT, "name", "desc", 0, 0, false,
        CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    commodityTypeId = UUID.randomUUID();
    commodityType.setId(commodityTypeId);

    when(commodityTypeRepository.save(any(CommodityType.class))).thenAnswer(
        invocation -> invocation.getArguments()[0]);
  }

  @Test
  public void shouldCreateNewCommodityType() {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(orderableRepository.findByProductCode(commodityType.getProductCode()))
        .willReturn(null);

    CommodityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityType.class);

    assertEquals(commodityType, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewCommodityTypeWithProgramOrderable() {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(orderableRepository.findByProductCode(commodityType.getProductCode()))
        .willReturn(null);

    ProgramOrderable programOrderable = mockProgramOrderable();
    commodityType.addToProgram(programOrderable);

    CommodityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityType.class);

    assertEquals(commodityType, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateCommodityType() {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(orderableRepository.findByProductCode(commodityType.getProductCode()))
        .willReturn(commodityType);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);

    CommodityType updatedCommodityType = CommodityType.newCommodityType(
        "code", UNIT, "update", "test", 0, 0, false,
        CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    updatedCommodityType.setId(commodityTypeId);

    CommodityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(updatedCommodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityType.class);

    assertEquals(commodityType, response);
    assertEquals(updatedCommodityType.getFullProductName(), response.getFullProductName());
    assertEquals(updatedCommodityType.getDescription(), response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdatingIfNotAlreadyACommodityType() {
    mockUserHasRight(ORDERABLES_MANAGE);

    given(orderableRepository.findByProductCode(commodityType.getProductCode()))
        .willReturn(commodityType);

    CommodityType updatedCommodityType = CommodityType.newCommodityType(
        "code", UNIT, "update", "test", 0, 0, false,
        CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    updatedCommodityType.setId(commodityTypeId);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(updatedCommodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutCommodityTypeIfUserHasNoRight() {
    mockUserHasNoRight(ORDERABLES_MANAGE);

    given(orderableRepository.findByProductCode(commodityType.getProductCode()))
        .willReturn(null);

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
  public void shouldUpdateTradeItemAssociations() {
    mockUserHasRight(ORDERABLES_MANAGE);
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);

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
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);

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
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);

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
    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);

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
    CommodityType parent = CommodityType.newCommodityType("XXX", "each", "parentProd",
        "desc", 10, 2, true, CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    parent.setId(parentId);
    commodityType.assignParent(parent);

    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);
    given(commodityTypeRepository.findOne(parentId))
        .willReturn(parent);

    CommodityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(commodityType)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(CommodityType.class);

    assertEquals(commodityType, response);
    assertEquals(commodityType.getParent().getId(), response.getParent().getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfParentNotFound() {
    mockUserHasRight(ORDERABLES_MANAGE);

    UUID parentId = UUID.randomUUID();
    CommodityType parent = CommodityType.newCommodityType("XXX", "each", "parentProd",
        "desc", 10, 2, true, CLASSIFICATION_SYS, CLASSIFICATION_SYS_ID);
    parent.setId(parentId);
    commodityType.assignParent(parent);

    given(commodityTypeRepository.findOne(commodityTypeId))
        .willReturn(commodityType);

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

  private ProgramOrderable mockProgramOrderable() {
    Program program = mockProgram();
    OrderableDisplayCategory category = mockOrderableDisplayCategory();
    return ProgramOrderable.createNew(
        program, category, commodityType, 1, false, false, 1,
        Money.of(CurrencyUnit.USD, 10.0), CurrencyUnit.USD);
  }

  private Program mockProgram() {
    Program program = new Program("programCode");
    program.setId(UUID.randomUUID());
    when(programReposiroty.findOne(program.getId())).thenReturn(program);
    return program;
  }

  private OrderableDisplayCategory mockOrderableDisplayCategory() {
    OrderableDisplayCategory category = OrderableDisplayCategory.createNew(
        Code.code("categoryCode"), new OrderedDisplayValue("name", 1));
    category.setId(UUID.randomUUID());
    when(orderableDisplayCategoryRepository.findOne(category.getId())).thenReturn(category);
    return category;
  }

  private TradeItem mockTradeItem() {
    UUID id = UUID.randomUUID();
    TradeItem tradeItem = TradeItem.newTradeItem("code " + id.toString(), UNIT,
        null, 0, 0, false);
    tradeItem.setId(id);
    when(tradeItemRepository.findOne(id)).thenReturn(tradeItem);
    return tradeItem;
  }

}
