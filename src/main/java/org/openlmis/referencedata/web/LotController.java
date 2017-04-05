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

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.LotDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.LotMessageKeys;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.openlmis.referencedata.validate.LotValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@Transactional
public class LotController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LotController.class);

  @Autowired
  private LotRepository lotRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private LotValidator validator;

  /**
   * Allows creating new Lots.
   *
   * @param lot a Lot bound to the request body.
   * @return the created Lot.
   */
  @RequestMapping(value = "/lots", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Lot createLot(@RequestBody Lot lot, BindingResult bindingResult) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    validator.validate(lot, bindingResult);
    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationMessageException(new Message(bindingResult.getFieldError().getCode(),
              bindingResult.getFieldError().getArguments()));
    }

    LOGGER.debug("Creating new Lot");
    lot.setId(null);
    lot = lotRepository.save(lot);
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
  public Lot updateLot(@RequestBody Lot lot, @PathVariable("id") UUID lotId,
                       BindingResult bindingResult) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    // ensure lot exists
    Lot existingLot = lotRepository.findOne(lotId);
    if (null == existingLot) {
      throw new NotFoundException(new Message(LotMessageKeys.ERROR_NOT_FOUND_WITH_ID, lotId));
    }

    validator.validate(lot, bindingResult);
    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationMessageException(new Message(bindingResult.getFieldError().getCode(),
              bindingResult.getFieldError().getArguments()));
    }

    LOGGER.debug("Updating Lot");
    lot = lotRepository.save(lot);
    return lot;
  }

  /**
   * Get chosen lot.
   *
   * @param lotId UUID of the lot to get.
   * @return chosen Lot.
   */
  @RequestMapping(value = "/lots/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public LotDto getLot(@PathVariable("id") UUID lotId) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    Lot lot = lotRepository.findOne(lotId);
    if (lot == null) {
      throw new NotFoundException(LotMessageKeys.ERROR_NOT_FOUND);
    }
    return exportToDto(lot);
  }

  /**
   * Retrieves all Lots matching given parameters.
   *
   * @return List of matched Lots.
   */
  @GetMapping("/lots/search")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<LotDto> searchLots(
      @RequestParam(value = "tradeIdemId", required = false) UUID tradeIdemId,
      @RequestParam(value = "expirationDate", required = false) ZonedDateTime expirationDate,
      @RequestParam(value = "lotCode", required = false) String lotCode) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    TradeItem tradeItem = null;
    if (isNotTrue(isNull(tradeIdemId))) {
      tradeItem = tradeItemRepository.findOne(tradeIdemId);
      if (isNull(tradeItem)) {
        throw new ValidationMessageException(
            new Message(TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID, tradeIdemId));
      }
    }

    return exportToDtos(lotRepository.search(tradeItem, expirationDate, lotCode));
  }

  private LotDto exportToDto(Lot lot) {
    LotDto lotDto = new LotDto();
    lot.export(lotDto);
    return lotDto;
  }

  private List<LotDto> exportToDtos(List<Lot> lots) {
    List<LotDto> lotsDto = new ArrayList<>(lots.size());
    for (Lot lot : lots) {
      lotsDto.add(exportToDto(lot));
    }
    return lotsDto;
  }
}
