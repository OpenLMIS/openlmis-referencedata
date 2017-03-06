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

import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.CommodityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

@RestController
public class CommodityTypeController extends BaseController {
  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private OrderableRepository repository;

  @Autowired
  private RightService rightService;

  /**
   * Add or update a commodity type
   * @param commodityType the commodity type to add or update.
   */
  @Transactional
  @RequestMapping(value = "/commodityTypes", method = RequestMethod.PUT)
  public CommodityType createOrUpdate(@RequestBody CommodityType commodityType) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    // if it already exists, update or fail if not already a CommodityType
    Orderable storedProduct = repository.findByProductCode(commodityType.getProductCode());
    if (null != storedProduct) {
      UUID productId = storedProduct.getId();
      if (null != commodityTypeRepository.findOne(productId)) {
        commodityType.setId(productId);
      } else {
        throw new ValidationMessageException(new Message(
            CommodityTypeMessageKeys.ERROR_NOT_A_COMMODITY_TYPE, commodityType.getProductCode()));
      }
    }

    repository.save(commodityType);
    return commodityType;
  }

  /**
   * Update the {@link TradeItem} that may fulfill for a {@link CommodityType}.
   * @param commodityTypeId the CommodityType's persistence Id.
   * @param tradeItemIds the persistence id's of the TradeItems
   * @return {@link org.springframework.http.HttpStatus#OK} if successful.
   * {@link org.springframework.http.HttpStatus#NOT_FOUND} if any of the given persistence ids
   *     are not found.
   */
  @Transactional
  @RequestMapping(value = "/commodityTypes/{id}/tradeItems", method = RequestMethod.PUT)
  public ResponseEntity<?> updateTradeItemAssociations(@PathVariable("id") UUID commodityTypeId,
                                          @RequestBody Set<UUID> tradeItemIds) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    // ensure trade item list isn't null
    if (null == tradeItemIds) {
      throw new ValidationMessageException(CommodityTypeMessageKeys.ERROR_TRADE_ITEMS_NULL);
    }

    // ensure commodity type exists
    CommodityType commodityType = commodityTypeRepository.findOne(commodityTypeId);
    if (null == commodityType) {
      throw new NotFoundException(CommodityTypeMessageKeys.ERROR_NOT_FOUND);
    }

    // create set of trade items from their ids, stop if any one is not found
    Set<TradeItem> tradeItems = new HashSet<>(tradeItemIds.size());
    for (UUID id : tradeItemIds) {
      TradeItem item = tradeItemRepository.findOne(id);
      if (null == item) {
        throw new NotFoundException(new Message(TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID, id));
      }

      tradeItems.add(item);
    }

    // update commodity type with new trade item association
    commodityType.setTradeItems(tradeItems);
    commodityTypeRepository.save(commodityType);

    return ResponseEntity.ok().build();
  }

  /**
   * Gets the TradeItem's persistence ids that may fulfill for the given CommodityType.
   * @param commodityTypeId persistence id of the CommodityType.
   * @return {@link org.springframework.http.HttpStatus#OK} and a list of persistence ids for the
   *      commodity type, or an empty list. {@link org.springframework.http.HttpStatus#NOT_FOUND} if
   *      there's no commodity type for the id given.
   */
  @Transactional
  @RequestMapping(value = "/commodityTypes/{id}/tradeItems", method = RequestMethod.GET)
  public ResponseEntity<?> getTradeItems(@PathVariable("id") UUID commodityTypeId) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    // ensure commodity type exists
    CommodityType commodityType = commodityTypeRepository.findOne(commodityTypeId);
    if (null == commodityType) {
      throw new NotFoundException(CommodityTypeMessageKeys.ERROR_NOT_FOUND);
    }

    Set<UUID> ids = new HashSet<>();
    List<TradeItem> items = tradeItemRepository.findForCommodityType(commodityType);
    for (TradeItem item : items) {
      ids.add(item.getId());
    }

    return ResponseEntity
        .ok()
        .body(ids);
  }
}
