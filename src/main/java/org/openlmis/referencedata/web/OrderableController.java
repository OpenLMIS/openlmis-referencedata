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

import static org.openlmis.referencedata.domain.RightName.ORDERABLES_MANAGE;

import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.service.OrderableService;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.openlmis.referencedata.validate.OrderableValidator;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class OrderableController extends BaseController {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(OrderableController.class);

  @Autowired
  private OrderableRepository repository;

  @Autowired
  private OrderableService orderableService;

  @Autowired
  private OrderableValidator validator;

  /**
   * Create an orderable.
   *
   * @return the orderable that was created.
   */
  @Transactional
  @PutMapping("/orderables")
  public OrderableDto create(@RequestBody OrderableDto orderableDto,
                                     BindingResult bindingResult) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);
    orderableDto.setId(null);
    validator.validate(orderableDto, bindingResult);
    throwValidationMessageExceptionIfErrors(bindingResult);

    Orderable orderable = Orderable.newInstance(orderableDto);

    return OrderableDto.newInstance(repository.save(orderable));
  }

  /**
   * Finds all orderables.
   *
   * @return a list of orderables
   */
  @GetMapping("/orderables")
  public Page<OrderableDto> findAll(Pageable pageable) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    List<Orderable> allOrderables = new ArrayList<>();
    for (Orderable product : repository.findAll()) {
      allOrderables.add(product);
    }

    return Pagination.getPage(OrderableDto.newInstance(allOrderables), pageable);
  }

  /**
   * Finds product with chosen id.
   *
   * @param productId id of the chosen product
   * @return chosen product
   */
  @GetMapping("/orderables/{id}")
  public OrderableDto getChosenOrderable(
      @PathVariable("id") UUID productId) {
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    Orderable orderable = repository.findOne(productId);
    if (orderable == null) {
      throw new NotFoundException(OrderableMessageKeys.ERROR_NOT_FOUND);
    } else {
      return OrderableDto.newInstance(orderable);
    }
  }

  /**
   * Get the audit information related to orderable.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/orderables/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getOrderableAuditLog(
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
    Orderable instance = repository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(OrderableMessageKeys.ERROR_NOT_FOUND);
    }

    String auditLogs = getAuditLog(Orderable.class, id, author, changedPropertyName, page,
        returnJson);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(returnJson ? MediaType.APPLICATION_JSON : MediaType.TEXT_PLAIN);

    return new ResponseEntity<>(auditLogs, headers, HttpStatus.OK);
  }

  /**
   * Finds orderables matching all of the provided parameters.
   *
   * @param queryParams request parameters (code, name, program, ids).
   * @param pageable object used to encapsulate the pagination related values: page and size.
   * @return a page of orderables
   */
  @PostMapping("/orderables/search")
  public Page<OrderableDto> search(@RequestBody Map<String, Object> queryParams,
                                   Pageable pageable) {
    XLOGGER.entry(queryParams, pageable);
    Profiler profiler = new Profiler("ORDERABLES_SEARCH");
    profiler.setLogger(XLOGGER);

    profiler.start("CHECK_ADMIN_RIGHT");
    rightService.checkAdminRight(ORDERABLES_MANAGE);

    profiler.start("ORDERABLE_SERVICE_SEARCH");
    List<Orderable> orderables = orderableService.searchOrderables(queryParams);

    profiler.start("ORDERABLE_PAGINATION");
    Page<OrderableDto> page = Pagination.getPage(OrderableDto.newInstance(orderables), pageable);

    profiler.stop().log();
    XLOGGER.exit(page);
    return page;
  }
}
