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

import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.IdealStockAmountCsvModel;
import org.openlmis.referencedata.dto.IdealStockAmountDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.openlmis.referencedata.web.csv.format.CsvFormatter;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FORMAT_NOT_ALLOWED;

@Controller
public class IdealStockAmountController extends BaseController {

  private static final String DISPOSITION_BASE = "attachment; filename=";
  private static final String FORMAT = "format";
  private static final String CSV = "csv";

  @Autowired
  private IdealStockAmountRepository repository;

  @Autowired
  private CsvFormatter csvFormatter;

  @Autowired
  private MessageService messageService;

  /**
   * Retrieves all Ideal Stock Amounts.
   *
   * @param pageable object used to encapsulate the pagination values: page and size.
   * @return Page of wanted Ideal Stock Amounts.
   */
  @GetMapping("/idealStockAmounts")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<IdealStockAmountDto> getAll(Pageable pageable) {
    Page<IdealStockAmount> itemsPage = repository.findAll(pageable);

    return Pagination.getPage(toDto(itemsPage.getContent()), pageable,
        itemsPage.getTotalElements());
  }

  /**
   * Downloads csv file with all Ideal Stock Amounts.
   */
  @GetMapping(value = "/idealStockAmounts", params = FORMAT)
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public void download(@RequestParam(FORMAT) String format,
                       HttpServletResponse response) throws IOException {
    rightService.checkAdminRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNTS_MANAGE);

    if (!CSV.equals(format)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          messageService.localize(new Message(ERROR_FORMAT_NOT_ALLOWED, format, CSV)).asMessage());
      return;
    }

    List<IdealStockAmountCsvModel> items = toCsvDto(repository.findAll());

    response.setContentType("text/csv");
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
        DISPOSITION_BASE + "ideal_stock_amounts.csv");

    try {
      csvFormatter.process(
          response.getOutputStream(), new ModelClass(IdealStockAmountCsvModel.class), items);
    } catch (IOException ex) {
      throw new ValidationMessageException(ex, MessageKeys.ERROR_IO, ex.getMessage());
    }
  }

  private IdealStockAmountCsvModel toCsvDto(IdealStockAmount isa) {
    IdealStockAmountCsvModel dto = new IdealStockAmountCsvModel();
    isa.export(dto);
    return dto;
  }

  private List<IdealStockAmountCsvModel> toCsvDto(Iterable<IdealStockAmount> items) {
    return StreamSupport
        .stream(items.spliterator(), false)
        .map(this::toCsvDto)
        .collect(Collectors.toList());
  }

  private IdealStockAmountDto toDto(IdealStockAmount isa) {
    IdealStockAmountDto dto = new IdealStockAmountDto();
    isa.export(dto);
    return dto;
  }

  private List<IdealStockAmountDto> toDto(Iterable<IdealStockAmount> items) {
    return StreamSupport
        .stream(items.spliterator(), false)
        .map(this::toDto)
        .collect(Collectors.toList());
  }
}
