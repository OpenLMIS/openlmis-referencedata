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
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.restassured.response.ValidatableResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.testbuilder.FacilityTypeApprovedProductsDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;

public class OrderableFulfillControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String CAN_BE_FULFILLED_BY_ME = ".canBeFulfilledByMe";
  private static final String CAN_FULFILL_FOR_ME_FIELD_NAME = ".canFulfillForMe";

  private Orderable orderable = new OrderableDataBuilder().build();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();

  private UUID tradeItemOrderableId = UUID.randomUUID();
  private UUID commodityTypeOrderableId = UUID.randomUUID();

  @Override
  @Before
  public void setUp() {
    super.setUp();
    given(orderableRepository.findAllLatest(any())).willReturn(getPage(orderable));
  }

  @Test
  public void shouldCreateResourceForTradeItem() {
    orderable.setId(tradeItemOrderableId);
    given(factory.createFor(eq(orderable), any(), any()))
        .willReturn(OrderableFulfill.ofTradeItem(commodityTypeOrderableId));

    String canFulfillForMeField = tradeItemOrderableId + CAN_FULFILL_FOR_ME_FIELD_NAME;
    String canBeFulfilledByMeField = tradeItemOrderableId + CAN_BE_FULFILLED_BY_ME;

    ValidatableResponse response = doRequest(null);
    response.body(canFulfillForMeField, hasSize(0));
    response.body(canBeFulfilledByMeField, hasSize(1));
    response.body(canBeFulfilledByMeField, hasItem(commodityTypeOrderableId.toString()));
  }

  @Test
  public void shouldCreateResourceForCommodityType() {
    orderable.setId(commodityTypeOrderableId);
    given(factory.createFor(eq(orderable), any(), any()))
        .willReturn(OrderableFulfill.ofCommodityType(tradeItemOrderableId));

    String canBeFulfilledByMeField = commodityTypeOrderableId + CAN_BE_FULFILLED_BY_ME;
    String canFulfillForMeField = commodityTypeOrderableId + CAN_FULFILL_FOR_ME_FIELD_NAME;

    ValidatableResponse response = doRequest(null);
    response.body(canBeFulfilledByMeField, hasSize(0));
    response.body(canFulfillForMeField, hasSize(1));
    response.body(canFulfillForMeField, hasItem(tradeItemOrderableId.toString()));
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNoOrderables() {
    given(orderableRepository.findAllLatest(any())).willReturn(new PageImpl<>(emptyList()));

    doRequest(null).body("isEmpty()", is(true));
    verifyZeroInteractions(factory);
  }

  @Test
  public void shouldCreateResourceBasingOnIds() {
    orderable.setId(commodityTypeOrderableId);

    given(orderableRepository.findAllLatestByIds(any(), any())).willReturn(getPage(orderable));
    given(factory.createFor(eq(orderable), any(), any()))
        .willReturn(OrderableFulfill.ofCommodityType(tradeItemOrderableId));

    String canFulfillForMeField = commodityTypeOrderableId + CAN_FULFILL_FOR_ME_FIELD_NAME;

    HashMap<String, Object> params = Maps.newHashMap();
    params.put("id", commodityTypeOrderableId);

    ValidatableResponse response = doRequest(params);
    response.body(canFulfillForMeField, hasItem(tradeItemOrderableId.toString()));

    verify(orderableRepository, times(0)).findAllLatest(any());
  }

  @Ignore
  @Test
  public void shouldCreateResourceBasingOnFacilityIdAndProgramIdParams() {
    orderable.setId(commodityTypeOrderableId);
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProductsDataBuilder()
        .withOrderableId(commodityTypeOrderableId).build();

    given(facilityTypeApprovedProductRepository
        .searchProducts(eq(facilityId), eq(singleton(programId)), any(), any(), eq(true), any(),
            any(),
            any()))
        .willReturn(getPage(ftap));
    given(orderableRepository.findAllLatestByIds(any(), any())).willReturn(getPage(orderable));
    given(factory.createFor(eq(orderable), any(), any()))
        .willReturn(OrderableFulfill.ofCommodityType(tradeItemOrderableId));

    String canFulfillForMeField = commodityTypeOrderableId + CAN_FULFILL_FOR_ME_FIELD_NAME;

    HashMap<String, Object> params = Maps.newHashMap();
    params.put("facilityId", facilityId);
    params.put("programId", programId);

    ValidatableResponse response = doRequest(params);
    response.body(canFulfillForMeField, hasItem(tradeItemOrderableId.toString()));

    verify(orderableRepository, times(0)).findAllLatest(any());
    verify(facilityTypeApprovedProductRepository)
        .searchProducts(
            eq(facilityId), eq(singleton(programId)), any(), any(), eq(true), any(), any(), any());
  }

  @SafeVarargs
  private final <T> Page<T> getPage(T... instance) {
    return new PageImpl<>(Lists.newArrayList(instance));
  }

  private ValidatableResponse doRequest(Map<String, ?> params) {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .queryParams(params != null ? params : Collections.emptyMap())
        .get("/api/orderableFulfills")
        .then()
        .statusCode(200);
  }

}
