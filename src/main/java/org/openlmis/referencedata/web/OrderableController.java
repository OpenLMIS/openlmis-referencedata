package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
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
public class OrderableController extends BaseController {

  @Autowired
  private OrderableRepository repository;

  /**
   * Finds all orderables.
   * @return a list of orderables
   */
  @RequestMapping("/orderables")
  public List<Orderable> findAll() {
    List<Orderable> allOrderables = new ArrayList<>();
    for (Orderable product:repository.findAll()) {
      allOrderables.add(product);
    }

    return allOrderables;
  }

  /**
   * Finds product with chosen id.
   * @param productId id of the chosen product
   * @return chosen product
   */
  @RequestMapping(value = "/orderables/{id}", method = RequestMethod.GET)
  public ResponseEntity<Orderable> getChosenOrderable(
      @PathVariable("id") UUID productId) {
    Orderable orderable = repository.findOne(productId);
    if (orderable == null) {
      throw new NotFoundException(OrderableMessageKeys.NOT_FOUND);
    } else {
      return new ResponseEntity<>(orderable, HttpStatus.OK);
    }
  }

}
