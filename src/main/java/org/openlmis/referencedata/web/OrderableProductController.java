package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.openlmis.referencedata.util.messagekeys.OrderableProductMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class OrderableProductController extends BaseController {

  @Autowired
  private OrderableProductRepository repository;

  /**
   * Finds all products.
   * @return a list of orderable products
   */
  @RequestMapping("/orderableProducts")
  public List<OrderableProduct> findAll() {
    List<OrderableProduct> allProducts = new ArrayList<>();
    for (OrderableProduct product:repository.findAll()) {
      allProducts.add(product);
    }

    return allProducts;
  }

  /**
   * Finds product with chosen id.
   * @param productId id of the chosen product
   * @return chosen product
   */
  @RequestMapping(value = "/orderableProducts/{id}", method = RequestMethod.GET)
  public ResponseEntity<OrderableProduct> getChosenOrderableProduct(
      @PathVariable("id") UUID productId) {
    OrderableProduct product = repository.findOne(productId);
    if (product == null) {
      throw new NotFoundException(OrderableProductMessageKeys.NOT_FOUND);
    } else {
      return new ResponseEntity<>(product, HttpStatus.OK);
    }
  }

}
