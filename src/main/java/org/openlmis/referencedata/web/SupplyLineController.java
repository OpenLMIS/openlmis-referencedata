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

import static org.openlmis.referencedata.domain.RightName.SUPPLY_LINES_MANAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.dto.SupplyLineDto;
import org.openlmis.referencedata.dto.SupplyLineSimpleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyLineRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.SupplyLineMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
public class SupplyLineController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SupplyLineController.class);

  @Autowired
  private SupplyLineService supplyLineService;

  @Autowired
  private SupplyLineRepository supplyLineRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Allows creating new supplyLines. If the id is specified, it will be ignored.
   *
   * @param supplyLineDto A supplyLine bound to the request body.
   * @return the created supplyLine.
   */
  @RequestMapping(value = "/supplyLines", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public SupplyLineDto createSupplyLine(@RequestBody SupplyLineDto supplyLineDto) {
    rightService.checkAdminRight(SUPPLY_LINES_MANAGE);
    LOGGER.debug("Creating new supplyLine");

    supplyLineDto.setId(null);
    SupplyLine supplyLine = SupplyLine.newSupplyLine(supplyLineDto);
    supplyLineRepository.save(supplyLine);
    LOGGER.debug("Created new supplyLine with id: " + supplyLine.getId());
    return exportToDto(supplyLine);
  }

  /**
   * Get all supplyLines.
   *
   * @return the SupplyLineDtos.
   */
  @RequestMapping(value = "/supplyLines", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SupplyLineDto> getAllSupplyLines() {

    Iterable<SupplyLine> supplyLines = supplyLineRepository.findAll();
    List<SupplyLineDto> supplyLineDtos = new ArrayList<>();

    for (SupplyLine supplyLine : supplyLines) {
      supplyLineDtos.add(exportToDto(supplyLine));
    }

    return supplyLineDtos;
  }

  /**
   * Allows updating supplyLines.
   *
   * @param supplyLineDto A supplyLineDto bound to the request body.
   * @param supplyLineId  UUID of supplyLine which we want to update.
   * @return the updated supplyLine.
   */
  @RequestMapping(value = "/supplyLines/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SupplyLineDto updateSupplyLine(@RequestBody SupplyLineDto supplyLineDto,
                                        @PathVariable("id") UUID supplyLineId) {
    rightService.checkAdminRight(SUPPLY_LINES_MANAGE);

    SupplyLine supplyLineToUpdate = supplyLineRepository.findOne(supplyLineId);
    if (supplyLineToUpdate == null) {
      supplyLineToUpdate = new SupplyLine();
      LOGGER.debug("Creating new supplyLine");
    } else {
      LOGGER.debug("Updating supplyLine with id: " + supplyLineId);
    }

    supplyLineToUpdate.updateFrom(SupplyLine.newSupplyLine(supplyLineDto));
    supplyLineRepository.save(supplyLineToUpdate);

    LOGGER.debug("Saved supplyLine with id: " + supplyLineToUpdate.getId());
    return exportToDto(supplyLineToUpdate);
  }

  /**
   * Get the audit information related to stock supply line.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/supplyLines/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getSupplyLineAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {
    rightService.checkAdminRight(SUPPLY_LINES_MANAGE);

    //Return a 404 if the specified instance can't be found
    SupplyLine instance = supplyLineRepository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(SupplyLineMessageKeys.ERROR_NOT_FOUND);
    }

    return getAuditLogResponse(SupplyLine.class, id, author, changedPropertyName, page,
        returnJson);
  }

  /**
   * Get chosen supplyLine.
   *
   * @param supplyLineId UUID of supplyLine which we want to get.
   * @return the SupplyLine.
   */
  @RequestMapping(value = "/supplyLines/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SupplyLineDto getSupplyLine(@PathVariable("id") UUID supplyLineId) {

    SupplyLine supplyLine = supplyLineRepository.findOne(supplyLineId);
    if (supplyLine == null) {
      throw new NotFoundException(SupplyLineMessageKeys.ERROR_NOT_FOUND);
    } else {
      return exportToDto(supplyLine);
    }
  }

  /**
   * Allows deleting supplyLine.
   *
   * @param supplyLineId UUID of supplyLine which we want to delete
   */
  @RequestMapping(value = "/supplyLines/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSupplyLine(@PathVariable("id") UUID supplyLineId) {
    rightService.checkAdminRight(SUPPLY_LINES_MANAGE);

    SupplyLine supplyLine = supplyLineRepository.findOne(supplyLineId);
    if (supplyLine == null) {
      throw new NotFoundException(SupplyLineMessageKeys.ERROR_NOT_FOUND);
    } else {
      supplyLineRepository.delete(supplyLine);
    }
  }

  /**
   * Retrieves page of Geographic Zones matching given parameters.
   *
   * @param queryParams request parameters (program, supervisoryNode, supplyingFacility).
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return Page of matched Supply Lines.
   */
  @RequestMapping(value = "/supplyLines/search", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<SupplyLineDto> searchSupplyLines(@RequestBody Map<String, Object> queryParams,
                                               Pageable pageable) {

    Profiler profiler = new Profiler("SEARCH_FOR_SUPPLY_LINES");
    profiler.setLogger(LOGGER);

    profiler.start("FIND_SUPPLY_LINES_IN_DB");
    Page<SupplyLine> page = supplyLineService.searchSupplyLines(queryParams, pageable);

    profiler.start("EXPORT_TO_DTO");
    Page<SupplyLineDto> dtosPage = exportToDto(page, pageable);

    profiler.stop().log();
    return dtosPage;
  }

  /**
   * Returns all Supply Lines with matched parameters.
   *
   * @param programId         program of searched Supply Lines.
   * @param supervisoryNodeId supervisory node of searched Supply Lines.
   * @return a list of all Supply Lines matching provided parameters.
   */
  @RequestMapping(value = "/supplyLines/searchByUUID", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SupplyLineSimpleDto> searchSupplyLinesByUuid(
      @RequestParam(value = "programId") UUID programId,
      @RequestParam(value = "supervisoryNodeId", required = false) UUID supervisoryNodeId,
      @RequestParam(value = "supplyingFacilityId", required = false) UUID supplyingFacilityId) {

    Program program = programRepository.findOne(programId);
    SupervisoryNode supervisoryNode = null != supervisoryNodeId
        ? supervisoryNodeRepository.findOne(supervisoryNodeId)
        : null;
    Facility supplyingFacility = null != supplyingFacilityId
        ? facilityRepository.findOne(supplyingFacilityId)
        : null;


    List<SupplyLine> resultSupplyLine = supplyLineService.searchSupplyLines(
        program, supervisoryNode, supplyingFacility
    );

    List<SupplyLineSimpleDto> result = new ArrayList<>();
    for (SupplyLine supplyLine : resultSupplyLine) {
      SupplyLineSimpleDto supplyLineSimpleDto = new SupplyLineSimpleDto();
      supplyLine.export(supplyLineSimpleDto);
      result.add(supplyLineSimpleDto);
    }

    return result;
  }

  private SupplyLineDto exportToDto(SupplyLine supplyLine) {
    SupplyLineDto supplyLineDto = null;

    if (supplyLine != null) {
      supplyLineDto = new SupplyLineDto();
      supplyLine.export(supplyLineDto);
    }

    return supplyLineDto;
  }

  private Page<SupplyLineDto> exportToDto(Page<SupplyLine> page, Pageable pageable) {
    List<SupplyLineDto> list = page.getContent().stream()
        .map(this::exportToDto).collect(Collectors.toList());
    return Pagination.getPage(list, pageable, page.getTotalElements());
  }
}
