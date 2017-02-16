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
