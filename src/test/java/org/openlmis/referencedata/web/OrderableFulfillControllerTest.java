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
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.util.Pagination.DEFAULT_PAGE_NUMBER;
import static org.openlmis.referencedata.web.OrderableFulfillController.COMMODITY_TYPE;
import static org.openlmis.referencedata.web.OrderableFulfillController.TRADE_ITEM;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.OrderableFulfill;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.testbuilder.CommodityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class OrderableFulfillControllerTest {

  @Mock
  private OrderableRepository orderableRepository;

  @Mock
  private TradeItemRepository tradeItemRepository;

  @Mock
  private CommodityTypeRepository commodityTypeRepository;

  @InjectMocks
  private OrderableFulfillController controller;

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
    when(orderableRepository.findAll(pageable)).thenReturn(getPage(tradeItemOrderable));
    when(tradeItemRepository.findOne(tradeItem.getId())).thenReturn(tradeItem);
    when(commodityTypeRepository.findAll(pageable)).thenReturn(getPage(commodityType));
    when(orderableRepository.findAllByIdentifier(COMMODITY_TYPE, commodityType.getId().toString()))
        .thenReturn(Lists.newArrayList(commodityTypeOrderable));

    Map<UUID, OrderableFulfill> response = controller.getOrderableFulfills(null);

    assertThat(response, hasKey(tradeItemOrderable.getId()));

    OrderableFulfill orderableFulfill = response.get(tradeItemOrderable.getId());
    assertThat(orderableFulfill.getCanFulfillForMe(), is(nullValue()));
    assertThat(orderableFulfill.getCanBeFulfilledByMe(), hasSize(1));
    assertThat(orderableFulfill.getCanBeFulfilledByMe(), hasItem(commodityTypeOrderable.getId()));
  }

  @Test
  public void shouldCreateResourceForCommodityType() {
    when(orderableRepository.findAll(pageable)).thenReturn(getPage(commodityTypeOrderable));
    when(commodityTypeRepository.findOne(commodityType.getId())).thenReturn(commodityType);
    when(tradeItemRepository.findAll(pageable)).thenReturn(getPage(tradeItem));
    when(orderableRepository.findAllByIdentifier(TRADE_ITEM, tradeItem.getId().toString()))
        .thenReturn(Lists.newArrayList(tradeItemOrderable));

    Map<UUID, OrderableFulfill> response = controller.getOrderableFulfills(null);

    assertThat(response, hasKey(commodityTypeOrderable.getId()));

    OrderableFulfill orderableFulfill = response.get(commodityTypeOrderable.getId());
    assertThat(orderableFulfill.getCanFulfillForMe(), hasSize(1));
    assertThat(orderableFulfill.getCanFulfillForMe(), hasItem(tradeItemOrderable.getId()));
    assertThat(orderableFulfill.getCanBeFulfilledByMe(), is(nullValue()));
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNoOrderables() {
    when(orderableRepository.findAll(pageable)).thenReturn(new PageImpl<>(emptyList()));

    Map<UUID, OrderableFulfill> response = controller.getOrderableFulfills(null);
    assertThat(response.size(), is(0));
  }

  @Test
  public void shouldReturnEmptyListIfOrderableNotHaveIdentifiers() {
    tradeItemOrderable = new OrderableDataBuilder().build();
    commodityTypeOrderable = new OrderableDataBuilder().build();

    when(orderableRepository.findAll(pageable))
        .thenReturn(getPage(tradeItemOrderable, commodityTypeOrderable));

    Map<UUID, OrderableFulfill> response = controller.getOrderableFulfills(null);
    assertThat(response.size(), is(0));
  }

  @SafeVarargs
  private final <T> Page<T> getPage(T... instance) {
    return new PageImpl<>(Lists.newArrayList(instance));
  }
}
