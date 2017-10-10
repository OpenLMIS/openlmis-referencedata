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
import org.openlmis.referencedata.dto.UploadResultDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.util.IdealStockAmountDtoBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.openlmis.referencedata.validate.CsvHeaderValidator;
import org.openlmis.referencedata.web.csv.format.CsvFormatter;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.openlmis.referencedata.web.csv.parser.CsvParser;
import org.openlmis.referencedata.web.csv.recordhandler.IdealStockAmountPersistenceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FORMAT_NOT_ALLOWED;

@Controller
public class IdealStockAmountController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdealStockAmountController.class);

  public static final String RESOURCE_PATH = "/idealStockAmounts";
  private static final String DISPOSITION_BASE = "attachment; filename=";
  private static final String FORMAT = "format";
  private static final String CSV = "csv";

  @Autowired
  private IdealStockAmountRepository repository;

  @Autowired
  private CsvFormatter csvFormatter;

  @Autowired
  private CsvParser csvParser;

  @Autowired
  private IdealStockAmountPersistenceHandler idealStockAmountPersistenceHandler;

  @Autowired
  private CsvHeaderValidator csvHeaderValidator;

  @Autowired
  private MessageService messageService;

  @Autowired
  private IdealStockAmountDtoBuilder isaDtoBuilder;

  /**
   * Retrieves all Ideal Stock Amounts.
   *
   * @param pageable object used to encapsulate the pagination values: page and size.
   * @return Page of wanted Ideal Stock Amounts.
   */
  @GetMapping(RESOURCE_PATH)
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
  @GetMapping(value = RESOURCE_PATH, params = FORMAT)
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public void download(@RequestParam(FORMAT) String format,
                       HttpServletResponse response) throws IOException {

    Profiler profiler = new Profiler("DOWNLOAD_IDEAL_STOCK_AMOUNTS");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_ADMIN");
    rightService.checkAdminRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNTS_MANAGE);

    profiler.start("CHECK_FORMAT");
    if (!CSV.equals(format)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          messageService.localize(new Message(ERROR_FORMAT_NOT_ALLOWED, format, CSV)).asMessage());
      return;
    }

    profiler.start("FIND_ALL_IDEAL_STOCK_AMOUNTS");
    Iterable<IdealStockAmount> list = repository.findAll();

    profiler.start("CONVERT_IDEAL_STOCK_AMOUNTS_TO_DTO");
    List<IdealStockAmountCsvModel> items = toCsvDto(list);

    response.setContentType("text/csv");
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
        DISPOSITION_BASE + "ideal_stock_amounts.csv");

    profiler.start("PARSE_IDEAL_STOCK_AMOUNTS_TO_CSV");
    try {
      csvFormatter.process(
          response.getOutputStream(), new ModelClass(IdealStockAmountCsvModel.class), items);
    } catch (IOException ex) {
      throw new ValidationMessageException(ex, MessageKeys.ERROR_IO, ex.getMessage());
    }
    profiler.stop().log();
  }

  /**
   * Uploads csv file and converts to domain object.
   *
   * @param file File in ".csv" format to upload.
   * @return number of uploaded records
   */
  @PostMapping(value = RESOURCE_PATH, params = FORMAT)
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public UploadResultDto upload(@RequestParam(FORMAT) String format,
                                @RequestPart("file") MultipartFile file) {
    Profiler profiler = new Profiler("UPLOAD_IDEAL_STOCK_AMOUNTS");
    profiler.setLogger(LOGGER);

    profiler.start("CHECK_ADMIN");
    rightService.checkAdminRight(RightName.SYSTEM_IDEAL_STOCK_AMOUNTS_MANAGE);

    profiler.start("CHECK_FORMAT");
    if (!CSV.equals(format)) {
      throw new NotFoundException(new Message(ERROR_FORMAT_NOT_ALLOWED, format, CSV));
    }

    profiler.start("VALIDATE_FILE");
    validateCsvFile(file);
    ModelClass modelClass = new ModelClass(IdealStockAmountCsvModel.class);

    profiler.start("PARSE_FILE");
    try {
      int result = csvParser.process(file.getInputStream(), modelClass,
          idealStockAmountPersistenceHandler, csvHeaderValidator);
      return new UploadResultDto(result);
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

  private List<IdealStockAmountDto> toDto(Iterable<IdealStockAmount> items) {
    return StreamSupport
        .stream(items.spliterator(), false)
        .map(isa -> isaDtoBuilder.build(isa))
        .collect(Collectors.toList());
  }
}