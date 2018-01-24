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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.domain.Orderable.COMMODITY_TYPE;
import static org.openlmis.referencedata.domain.Orderable.TRADE_ITEM;
import static org.openlmis.referencedata.util.Pagination.DEFAULT_PAGE_NUMBER;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
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

@RunWith(MockitoJUnitRunner.class)
public class OrderableFulfillFactoryTest {

  @Mock
  private OrderableRepository orderableRepository;

  @Mock
  private TradeItemRepository tradeItemRepository;

  @Mock
  private CommodityTypeRepository commodityTypeRepository;

  @InjectMocks
  private OrderableFulfillFactory factory;

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
    when(tradeItemRepository.findOne(tradeItem.getId())).thenReturn(tradeItem);
    when(commodityTypeRepository.findAll(pageable)).thenReturn(getPage(commodityType));
    when(orderableRepository.findAllByIdentifier(COMMODITY_TYPE, commodityType.getId().toString()))
        .thenReturn(Lists.newArrayList(commodityTypeOrderable));

    OrderableFulfill response = factory.createFor(tradeItemOrderable);
    assertThat(response.getCanFulfillForMe(), hasSize(0));
    assertThat(response.getCanBeFulfilledByMe(), hasSize(1));
    assertThat(response.getCanBeFulfilledByMe(), hasItem(commodityTypeOrderable.getId()));
  }

  @Test
  public void shouldCreateResourceForCommodityType() {
    when(commodityTypeRepository.findOne(commodityType.getId())).thenReturn(commodityType);
    when(tradeItemRepository.findAll(pageable)).thenReturn(getPage(tradeItem));
    when(orderableRepository.findAllByIdentifier(TRADE_ITEM, tradeItem.getId().toString()))
        .thenReturn(Lists.newArrayList(tradeItemOrderable));

    OrderableFulfill response = factory.createFor(commodityTypeOrderable);
    assertThat(response.getCanFulfillForMe(), hasSize(1));
    assertThat(response.getCanFulfillForMe(), hasItem(tradeItemOrderable.getId()));
    assertThat(response.getCanBeFulfilledByMe(), hasSize(0));
  }

  @Test
  public void shouldNotCreateResourceIfThereAreNoIdentifiers() {
    OrderableFulfill response = factory.createFor(new OrderableDataBuilder().build());
    assertThat(response, is(nullValue()));
  }

  @SafeVarargs
  private final <T> Page<T> getPage(T... instance) {
    return new PageImpl<>(Lists.newArrayList(instance));
  }
}
