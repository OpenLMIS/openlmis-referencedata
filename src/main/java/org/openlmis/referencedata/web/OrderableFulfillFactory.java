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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.openlmis.referencedata.domain.Orderable.COMMODITY_TYPE;
import static org.openlmis.referencedata.domain.Orderable.TRADE_ITEM;
import static org.openlmis.referencedata.util.Pagination.handlePage;

import com.google.common.collect.Lists;

import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderableFulfillFactory {

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  /**
   * Create new instance of {@link OrderableFulfill} for the given orderable.
   */
  public OrderableFulfill createFor(Orderable orderable) {
    String tradeItemId = orderable.getTradeItemIdentifier();

    if (isNotBlank(tradeItemId)) {
      return createForTradeItem(tradeItemId);
    }

    String commodityTypeId = orderable.getCommodityTypeIdentifier();

    if (isNotBlank(commodityTypeId)) {
      return createForCommodityType(commodityTypeId);
    }

    return null;
  }

  private OrderableFulfill createForTradeItem(String id) {
    TradeItem tradeItem = tradeItemRepository.findOne(UUID.fromString(id));

    List<UUID> canBeFulfilledByMe = Lists.newArrayList();
    handlePage(
        commodityTypeRepository::findAll,
        commodityType -> {
          if (tradeItem.canFulfill(commodityType)) {
            setList(canBeFulfilledByMe, COMMODITY_TYPE, commodityType);
          }
        }
    );

    return OrderableFulfill.ofTradeIdem(canBeFulfilledByMe);
  }

  private OrderableFulfill createForCommodityType(String id) {
    CommodityType commodityType = commodityTypeRepository.findOne(UUID.fromString(id));

    List<UUID> canFulfillForMe = Lists.newArrayList();
    handlePage(
        tradeItemRepository::findAll,
        tradeItem -> {
          if (tradeItem.canFulfill(commodityType)) {
            setList(canFulfillForMe, TRADE_ITEM, tradeItem);
          }
        }
    );

    return OrderableFulfill.ofCommodityType(canFulfillForMe);
  }

  private void setList(List<UUID> list, String key, BaseEntity entity) {
    List<Orderable> orderables = orderableRepository
        .findAllByIdentifier(key, entity.getId().toString());

    orderables.forEach(item -> list.add(item.getId()));
  }

}
