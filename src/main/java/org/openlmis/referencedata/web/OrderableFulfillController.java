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

import static org.openlmis.referencedata.util.Pagination.handlePage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.UuidUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
public class OrderableFulfillController extends BaseController {

  private static final XLogger XLOGGER = XLoggerFactory
          .getXLogger(OrderableFulfillController.class);

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private OrderableFulfillFactory orderableFulfillFactory;

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
    Profiler profiler = new Profiler("GET_ORDERABLE_FULFILLS");
    profiler.setLogger(XLOGGER);

    Set<UUID> ids = UuidUtil.getIds(requestParams);
    Map<UUID, OrderableFulfill> map = Maps.newHashMap();

    Iterable<TradeItem> tradeItems = tradeItemRepository.findAll();
    Iterable<CommodityType> commodityTypes = commodityTypeRepository.findAll();

    handlePage(
        pageable -> getOrderables(ids, profiler, pageable),
        orderable -> addEntry(map, orderable, tradeItems, commodityTypes)
    );

    profiler.stop().log();
    return map;
  }

  private Page<Orderable> getOrderables(Set<UUID> ids, Profiler profiler, Pageable pageable) {
    profiler.start("GET_ORDERABLES");
    return ids.isEmpty()
        ? orderableRepository.findAllLatest(pageable)
        : orderableRepository.findAllLatestByIds(ids, pageable);
  }

  private void addEntry(Map<UUID, OrderableFulfill> map, Orderable orderable,
                        Iterable<TradeItem> tradeItems, Iterable<CommodityType> commodityTypes) {
    Optional
        .ofNullable(orderableFulfillFactory.createFor(orderable, tradeItems, commodityTypes))
        .ifPresent(resource -> map.put(orderable.getId(), resource));
  }

}
