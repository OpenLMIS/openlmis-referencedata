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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.openlmis.referencedata.util.Pagination.DEFAULT_PAGE_NUMBER;

import com.google.common.collect.Lists;

import com.jayway.restassured.response.ValidatableResponse;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableFulfill;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

public class OrderableFulfillControllerIntegrationTest extends BaseWebIntegrationTest {

  @MockBean
  private OrderableFulfillFactory factory;

  private Pageable pageable = new PageRequest(DEFAULT_PAGE_NUMBER, 2000);
  private Orderable orderable = new OrderableDataBuilder().build();

  private UUID tradeItemOrderableId = UUID.randomUUID();
  private UUID commodityTypeOrderableId = UUID.randomUUID();

  @Override
  @Before
  public void setUp() {
    super.setUp();
    given(orderableRepository.findAll(pageable)).willReturn(getPage(orderable));
  }

  @Test
  public void shouldCreateResourceForTradeItem() {
    orderable.setId(tradeItemOrderableId);
    given(factory.createFor(orderable))
        .willReturn(OrderableFulfill.ofTradeIdem(commodityTypeOrderableId));

    String canFulfillForMeField = tradeItemOrderableId + ".canFulfillForMe";
    String canBeFulfilledByMeField = tradeItemOrderableId + ".canBeFulfilledByMe";

    ValidatableResponse response = doRequest();
    response.body(canFulfillForMeField, hasSize(0));
    response.body(canBeFulfilledByMeField, hasSize(1));
    response.body(canBeFulfilledByMeField, hasItem(commodityTypeOrderableId.toString()));
  }

  @Test
  public void shouldCreateResourceForCommodityType() {
    orderable.setId(commodityTypeOrderableId);
    given(factory.createFor(orderable))
        .willReturn(OrderableFulfill.ofCommodityType(tradeItemOrderableId));

    String canBeFulfilledByMeField = commodityTypeOrderableId + ".canBeFulfilledByMe";
    String canFulfillForMeField = commodityTypeOrderableId + ".canFulfillForMe";

    ValidatableResponse response = doRequest();
    response.body(canBeFulfilledByMeField, hasSize(0));
    response.body(canFulfillForMeField, hasSize(1));
    response.body(canFulfillForMeField, hasItem(tradeItemOrderableId.toString()));
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNoOrderables() {
    given(orderableRepository.findAll(pageable)).willReturn(new PageImpl<>(emptyList()));

    doRequest().body("isEmpty()", is(true));
    verifyZeroInteractions(factory);
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
