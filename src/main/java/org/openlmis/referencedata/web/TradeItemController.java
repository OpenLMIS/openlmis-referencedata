package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TradeItemController {

  @Autowired
  private OrderableProductRepository repository;

  /**
   * Create or update a trade item.
   * @return the trade item that was created or updated.
   */
  @RequestMapping(value = "/tradeItems", method = RequestMethod.PUT)
  public TradeItem createOrUpdate(@RequestBody TradeItem tradeItem) {
    // if it already exists, update or fail if not already a GlobalProduct
    OrderableProduct storedProduct = repository.findByProductCode(tradeItem.getProductCode());
    if ( null != storedProduct ) {
      tradeItem.setId(storedProduct.getId());
    }

    repository.save(tradeItem);
    return tradeItem;
  }
}
