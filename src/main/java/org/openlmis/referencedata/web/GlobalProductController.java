package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalProductController {

  @Autowired
  private OrderableProductRepository repository;

  /**
   * Add or update a global product
   * @param globalProduct the global product to add or update.
   */
  @Transactional
  @RequestMapping(value = "/globalProducts", method = RequestMethod.PUT)
  public void createOrUpdate(@RequestBody GlobalProduct globalProduct) {
    // if it already exists, update or fail if not already a GlobalProduct
    OrderableProduct storedProduct = repository.findByProductCode(globalProduct.getProductCode());
    if ( null != storedProduct ) {
      globalProduct.setId(storedProduct.getId());
    }

    repository.save(globalProduct);
  }
}
