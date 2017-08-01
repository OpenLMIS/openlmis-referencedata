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
import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.LotDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.LotMessageKeys;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.openlmis.referencedata.validate.LotValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.time.LocalDate;
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
   * @param lotDto a LotDto bound to the request body.
   * @return the created LotDto.
   */
  @RequestMapping(value = "/lots", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public LotDto createLot(@RequestBody LotDto lotDto, BindingResult bindingResult) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    validator.validate(lotDto, bindingResult);
    throwValidationMessageExceptionIfErrors(bindingResult);
    TradeItem tradeItem = tradeItemRepository.findOne(lotDto.getTradeItemId());
    Lot lotToSave = Lot.newLot(lotDto, tradeItem);
    lotToSave.setId(null);

    LOGGER.debug("Creating new Lot");
    lotToSave = lotRepository.save(lotToSave);
    return exportToDto(lotToSave);
  }

  /**
   * Allows updating Lots.
   *
   * @param lotDto   a LotDto bound to the request body.
   * @param lotId the UUID of Lot which we want to update.
   * @return the updated LotDto.
   */
  @RequestMapping(value = "/lots/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public LotDto updateLot(@RequestBody LotDto lotDto, @PathVariable("id") UUID lotId,
                       BindingResult bindingResult) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    Lot existingLot = lotRepository.findOne(lotId);
    if (existingLot == null) {
      throw new NotFoundException(new Message(LotMessageKeys.ERROR_NOT_FOUND_WITH_ID, lotId));
    }
    validator.validate(lotDto, bindingResult);
    throwValidationMessageExceptionIfErrors(bindingResult);
    TradeItem tradeItem = tradeItemRepository.findOne(lotDto.getTradeItemId());
    Lot lotToSave = Lot.newLot(lotDto, tradeItem);
    lotToSave.setId(lotId);

    LOGGER.debug("Updating Lot");
    lotToSave = lotRepository.save(lotToSave);
    return exportToDto(lotToSave);
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
   * For lotCode: finds any values that have entered string value
   * in any position of searched field. Not case sensitive.
   * Other fields: entered string value must equal to searched value.
   *
   * @param tradeIdemId UUID of trade item associated with Lot.
   * @param expirationDate Lot expiration date.
   * @param lotCode Lot code.
   * @param pageable Pageable object that allows client to optionally add "page" (page number).
   * @return List of matched Lots.
   */
  @GetMapping("/lots/search")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<LotDto> searchLots(
      @RequestParam(value = "tradeIdemId", required = false) UUID tradeIdemId,
      @RequestParam(value = "expirationDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate,
      @RequestParam(value = "lotCode", required = false) String lotCode,
      Pageable pageable) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    TradeItem tradeItem = null;
    if (null != tradeIdemId) {
      tradeItem = tradeItemRepository.findOne(tradeIdemId);
      if (isNull(tradeItem)) {
        throw new ValidationMessageException(
            new Message(TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID, tradeIdemId));
      }
    }

    List<LotDto> foundLotDtos =
        exportToDtos(lotRepository.search(tradeItem, expirationDate, lotCode));

    return Pagination.getPage(foundLotDtos, pageable);

  }

  /**
   * Get the audit information related to lot.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/lots/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getLotAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {

    rightService.checkAdminRight(ORDERABLES_MANAGE);

    //Return a 404 if the specified instance can't be found
    Lot instance = lotRepository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(LotMessageKeys.ERROR_NOT_FOUND);
    }

    return getAuditLogResponse(Lot.class, id, author, changedPropertyName, page, returnJson);
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
