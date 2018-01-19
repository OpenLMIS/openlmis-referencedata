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
import static org.openlmis.referencedata.util.Pagination.handlePage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.OrderableFulfill;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@Transactional
public class OrderableFulfillController extends BaseController {
  static final String TRADE_ITEM = "tradeItem";
  static final String COMMODITY_TYPE = "commodityType";

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  /**
   * Gets orderable fulfills.
   */
  @GetMapping("/orderableFulfills")
  @ResponseStatus(HttpStatus.OK)
  public Map<UUID, OrderableFulfill> getOrderableFulfills(
      @RequestParam MultiValueMap<String, Object> requestParams) {
    Set<UUID> ids = UuidUtil.getIds(requestParams);
    Map<UUID, OrderableFulfill> map = Maps.newHashMap();

    handlePage(
        pageable -> ids.isEmpty()
            ? orderableRepository.findAll(pageable)
            : orderableRepository.findAllByIds(ids, pageable),
        elem -> addEntry(map, elem)
    );

    return map;
  }

  private void addEntry(Map<UUID, OrderableFulfill> map, Orderable orderable) {
    Optional
        .ofNullable(createResource(orderable))
        .ifPresent(resource -> map.put(orderable.getId(), resource));
  }

  private OrderableFulfill createResource(Orderable orderable) {
    String tradeItemId = orderable.getIdentifiers().get(TRADE_ITEM);

    if (isNotBlank(tradeItemId)) {
      return createResourceForTradeItem(tradeItemId);
    }

    String commodityTypeId = orderable.getIdentifiers().get(COMMODITY_TYPE);

    if (isNotBlank(commodityTypeId)) {
      return createResourceForCommodityType(commodityTypeId);
    }

    return null;
  }

  private OrderableFulfill createResourceForTradeItem(String id) {
    TradeItem tradeItem = tradeItemRepository.findOne(UUID.fromString(id));

    List<UUID> canBeFulfilledByMe = Lists.newArrayList();
    handlePage(
        commodityTypeRepository::findAll,
        elem -> {
          if (tradeItem.canFulfill(elem)) {
            setList(canBeFulfilledByMe, COMMODITY_TYPE, elem);
          }
        }
    );

    return new OrderableFulfill(null, canBeFulfilledByMe);
  }

  private OrderableFulfill createResourceForCommodityType(String id) {
    CommodityType commodityType = commodityTypeRepository.findOne(UUID.fromString(id));

    List<UUID> canFulfillForMe = Lists.newArrayList();
    handlePage(
        tradeItemRepository::findAll,
        elem -> {
          if (elem.canFulfill(commodityType)) {
            setList(canFulfillForMe, TRADE_ITEM, elem);
          }
        }
    );

    return new OrderableFulfill(canFulfillForMe, null);
  }

  private void setList(List<UUID> list, String key, BaseEntity entity) {
    List<Orderable> orderables = orderableRepository
        .findAllByIdentifier(key, entity.getId().toString());

    orderables.forEach(item -> list.add(item.getId()));
  }

}
