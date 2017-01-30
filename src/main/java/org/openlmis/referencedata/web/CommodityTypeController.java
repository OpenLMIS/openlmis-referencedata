package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
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

@RestController
public class CommodityTypeController extends BaseController {
  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private OrderableRepository repository;

  /**
   * Add or update a commodity type
   * @param commodityType the commodity type to add or update.
   */
  @Transactional
  @RequestMapping(value = "/commodityTypes", method = RequestMethod.PUT)
  public CommodityType createOrUpdate(@RequestBody CommodityType commodityType) {
    // if it already exists, update or fail if not already a CommodityType
    Orderable storedProduct = repository.findByProductCode(commodityType.getProductCode());
    if (null != storedProduct) {
      commodityType.setId(storedProduct.getId());
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
