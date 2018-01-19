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

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.openlmis.referencedata.util.Pagination.DEFAULT_PAGE_NUMBER;
import static org.openlmis.referencedata.web.OrderableFulfillController.COMMODITY_TYPE;
import static org.openlmis.referencedata.web.OrderableFulfillController.TRADE_ITEM;

import com.google.common.collect.Lists;

import com.jayway.restassured.response.ValidatableResponse;

import org.junit.Test;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.testbuilder.CommodityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

public class OrderableFulfillControllerIntegrationTest extends BaseWebIntegrationTest {

  private CommodityType commodityType = new CommodityTypeDataBuilder().build();
  private TradeItem tradeItem = new TradeItemDataBuilder()
      .withClassification(commodityType)
      .build();

  private Orderable tradeItemOrderable = new OrderableDataBuilder()
      .withIdentifier(TRADE_ITEM, tradeItem.getId())
      .build();

  private Orderable commodityTypeOrderable = new OrderableDataBuilder()
      .withIdentifier(COMMODITY_TYPE, commodityType.getId())
      .build();

  private Pageable pageable = new PageRequest(DEFAULT_PAGE_NUMBER, 2000);

  @Test
  public void shouldCreateResourceForTradeItem() {
    given(orderableRepository.findAll(pageable)).willReturn(getPage(tradeItemOrderable));
    given(tradeItemRepository.findOne(tradeItem.getId())).willReturn(tradeItem);
    given(commodityTypeRepository.findAll(pageable)).willReturn(getPage(commodityType));
    given(orderableRepository.findAllByIdentifier(COMMODITY_TYPE, commodityType.getId().toString()))
        .willReturn(Lists.newArrayList(commodityTypeOrderable));

    String canFulfillForMeField = tradeItemOrderable.getId() + ".canFulfillForMe";
    String canBeFulfilledByMeField = tradeItemOrderable.getId() + ".canBeFulfilledByMe";

    doRequest()
        .body(canFulfillForMeField, is(nullValue()))
        .body(canBeFulfilledByMeField, hasSize(1))
        .body(canBeFulfilledByMeField, hasItem(commodityTypeOrderable.getId().toString()));
  }

  @Test
  public void shouldCreateResourceForCommodityType() {
    given(orderableRepository.findAll(pageable)).willReturn(getPage(commodityTypeOrderable));
    given(commodityTypeRepository.findOne(commodityType.getId())).willReturn(commodityType);
    given(tradeItemRepository.findAll(pageable)).willReturn(getPage(tradeItem));
    given(orderableRepository.findAllByIdentifier(TRADE_ITEM, tradeItem.getId().toString()))
        .willReturn(Lists.newArrayList(tradeItemOrderable));

    String canBeFulfilledByMeField = commodityTypeOrderable.getId() + ".canBeFulfilledByMe";
    String canFulfillForMeField = commodityTypeOrderable.getId() + ".canFulfillForMe";

    doRequest()
        .body(canBeFulfilledByMeField, is(nullValue()))
        .body(canFulfillForMeField, hasSize(1))
        .body(canFulfillForMeField, hasItem(tradeItemOrderable.getId().toString()));
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNoOrderables() {
    given(orderableRepository.findAll(pageable)).willReturn(new PageImpl<>(emptyList()));

    doRequest().body("isEmpty()", is(true));
  }

  @Test
  public void shouldReturnEmptyListIfOrderableNotHaveIdentifiers() {
    tradeItemOrderable = new OrderableDataBuilder().build();
    commodityTypeOrderable = new OrderableDataBuilder().build();

    given(orderableRepository.findAll(pageable))
        .willReturn(getPage(tradeItemOrderable, commodityTypeOrderable));

    doRequest().body("isEmpty()", is(true));
  }

  @SafeVarargs
  private final <T> Page<T> getPage(T... instance) {
    return new PageImpl<>(Lists.newArrayList(instance));
  }

  private ValidatableResponse doRequest() {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get("/api/orderableFulfills")
        .then()
        .statusCode(200);
  }

}
