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

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.repository.LotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

@Controller
@Transactional
public class LotController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LotController.class);

  @Autowired
  private LotRepository lotRepository;

  /**
   * Allows creating new Lots.
   *
   * @param lot a Lot bound to the request body.
   * @return the created Lot.
   */
  @RequestMapping(value = "/lots", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Lot createLot(@RequestBody Lot lot) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    LOGGER.debug("Creating new Lot");
    // Ignore provided id
    lot.setId(null);
    lotRepository.save(lot);
    return lot;
  }

  /**
   * Allows updating Lots.
   *
   * @param lot   a Lot bound to the request body.
   * @param lotId the UUID of Lot which we want to update.
   * @return the updated Lot.
   */
  @RequestMapping(value = "/lots/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Lot updateLot(@RequestBody Lot lot, @PathVariable("id") UUID lotId) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    LOGGER.debug("Updating Lot");
    lotRepository.save(lot);
    return lot;
  }
}
