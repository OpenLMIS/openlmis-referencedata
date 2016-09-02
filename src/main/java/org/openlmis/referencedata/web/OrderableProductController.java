package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.repository.OrderableProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderableProductController {
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
}
