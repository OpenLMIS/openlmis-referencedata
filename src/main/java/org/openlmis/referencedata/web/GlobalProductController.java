package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.GlobalProductRepository;
import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
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
public class GlobalProductController {
  @Autowired
  private GlobalProductRepository globalProductRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private OrderableProductRepository repository;

  /**
   * Add or update a global product
   * @param globalProduct the global product to add or update.
   */
  @Transactional
  @RequestMapping(value = "/globalProducts", method = RequestMethod.PUT)
  public GlobalProduct createOrUpdate(@RequestBody GlobalProduct globalProduct) {
    // if it already exists, update or fail if not already a GlobalProduct
    OrderableProduct storedProduct = repository.findByProductCode(globalProduct.getProductCode());
    if ( null != storedProduct ) {
      globalProduct.setId(storedProduct.getId());
    }

    repository.save(globalProduct);
    return globalProduct;
  }

  /**
   * Update the {@link TradeItem} that may fulfill for a {@link GlobalProduct}.
   * @param globalProductId the GlobalProduct's persistence Id.
   * @param tradeItemIds the persistence id's of the TradeItems
   * @return {@link org.mortbay.jetty.HttpStatus#OK} if succesful.
   * {@link org.springframework.http.HttpStatus#NOT_FOUND} if any of the given persistence ids
   *     are not found.
   */
  @Transactional
  @RequestMapping(value = "/globalProducts/{id}/tradeItems", method = RequestMethod.PUT)
  public ResponseEntity<?> updateTradeItemAssociations(@PathVariable("id") UUID globalProductId,
                                          @RequestBody Set<UUID> tradeItemIds) {

    // ensure trade item list isn't null
    if (null == tradeItemIds) {
      return ResponseEntity.badRequest().build();
    }

    // ensure global product exists
    GlobalProduct globalProduct = globalProductRepository.findOne(globalProductId);
    if (null == globalProduct) {
      return ResponseEntity.notFound().build();
    }

    // create set of trade items from their ids, stop if any one is not found
    Set<TradeItem> tradeItems = new HashSet<>(tradeItemIds.size());
    for (UUID id : tradeItemIds) {
      TradeItem item = tradeItemRepository.findOne(id);
      if (null == item) {
        return ResponseEntity.notFound().build();
      }

      tradeItems.add(item);
    }

    // update global product with new trade item association
    globalProduct.setTradeItems(tradeItems);

    return ResponseEntity.ok().build();
  }

  /**
   * Gets the TradeItem's persistence ids that may fulfill for the given GlobalProduct.
   * @param globalProductId persistence id of the GlobalProduct.
   * @return {@link org.springframework.http.HttpStatus#OK} and a list of persistence ids for the
   *      global product, or an empty list. {@link org.springframework.http.HttpStatus#NOT_FOUND} if
   *      there's no global product for the id given.
   */
  @Transactional
  @RequestMapping(value = "/globalProducts/{id}/tradeItems", method = RequestMethod.GET)
  public ResponseEntity<?> getTradeItems(@PathVariable("id") UUID globalProductId) {
    // ensure global product exists
    GlobalProduct globalProduct = globalProductRepository.findOne(globalProductId);
    if (null == globalProduct) {
      return ResponseEntity.notFound().build();
    }

    Set<UUID> ids = new HashSet<>();
    List<TradeItem> items = tradeItemRepository.findForGlobalProduct(globalProduct);
    for (TradeItem item : items) {
      ids.add(item.getId());
    }

    return ResponseEntity
        .ok()
        .body(ids);
  }
}
