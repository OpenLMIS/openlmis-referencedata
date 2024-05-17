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

import java.util.UUID;

import static org.openlmis.referencedata.domain.RightName.UNIT_OF_ORDERABLES_MANAGE;

import org.openlmis.referencedata.domain.UnitOfOrderable;
import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.UnitOfOrderableRepository;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UnitOfOrderableController.RESOURCE_PATH)
public class UnitOfOrderableController extends BaseController {
  public static final String RESOURCE_PATH = BaseController.API_PATH + "/unitOfOrderables";

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(UnitOfOrderableController.class);

  @Autowired
  private UnitOfOrderableRepository unitOfOrderableRepository;

  /**
   * REST endpoint to get paginated UnitOfOrderable.
   *
   * @param pageable the requested page details, not null
   * @return a list of Unit Of Orderable Dtos, never null
   */
  @GetMapping
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public Page<UnitOfOrderableDto> getUnits(Pageable pageable) {
    final Page<UnitOfOrderable> unitsPage = unitOfOrderableRepository.findAll(pageable);
    return Pagination.getPage(
        UnitOfOrderableDto.newInstances(unitsPage.toList()),
        pageable,
        unitsPage.getTotalElements());
  }

  /**
   * Adding new UnitOfOrderable. If the id is specified, it will be ignored.
   *
   * @param unitOfOrderableDto A unitOfOrderable bound to the request body.
   * @return the created unitOfOrderable.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UnitOfOrderableDto createUnitOfOrderable(
      @RequestBody UnitOfOrderableDto unitOfOrderableDto) {
    rightService.checkAdminRight(UNIT_OF_ORDERABLES_MANAGE);
    XLOGGER.debug("Creating new unitOfOrderable");

    unitOfOrderableDto.setId(null);
    UnitOfOrderable unitOfOrderable = UnitOfOrderable.newInstance(unitOfOrderableDto);
    unitOfOrderableRepository.save(unitOfOrderable);
    XLOGGER.debug("Created new unitOfOrderable with id: " + unitOfOrderable.getId());
    return exportToDto(unitOfOrderable);
  }

  /**
   * Updating or creating (if not found) UnitOfOrderables.
   *
   * @param unitOfOrderableDto A unitOfOrderableDto bound to the request body.
   * @param id                 UUID of unitOfOrderable which we want to update.
   * @return the updated unitOfOrderable.
   */
  @PutMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public UnitOfOrderableDto updateUnitOfOrderable(
      @RequestBody UnitOfOrderableDto unitOfOrderableDto,
      @PathVariable UUID id) {
    rightService.checkAdminRight(UNIT_OF_ORDERABLES_MANAGE);

    UnitOfOrderable unitOfOrderableToUpdate = unitOfOrderableRepository
        .findById(id)
        .orElse(null);
    if (unitOfOrderableToUpdate == null) {
      unitOfOrderableToUpdate = new UnitOfOrderable();
      XLOGGER.debug("Creating new unitOfOrderable");
    } else {
      XLOGGER.debug("Updating unitOfOrderable with id: " + id);
    }

    unitOfOrderableToUpdate.updateFrom(UnitOfOrderable.newInstance(unitOfOrderableDto));
    unitOfOrderableRepository.save(unitOfOrderableToUpdate);

    XLOGGER.debug("Saved unitOfOrderable with id: " + unitOfOrderableToUpdate.getId());
    return exportToDto(unitOfOrderableToUpdate);
  }

  /**
   * Get the audit information related to units of orderable.
   *
   * @param author              The author of the changes which should be returned.
   *                            If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *                            If null or empty, changes associated with any and all properties are
   *                            returned.
   * @param page                A Pageable object that allows client to optionally add "page"
   *                            (page number) and "size" (page size) query parameters to the
   *                            request.
   */
  @GetMapping("{id}/auditLog")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> getUnitOfOrderableAuditLog(
      @PathVariable UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
      String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
      boolean returnJson,
      Pageable page) {
    rightService.checkAdminRight(UNIT_OF_ORDERABLES_MANAGE);

    //Return a 404 if the specified instance can't be found
    UnitOfOrderable instance = unitOfOrderableRepository.findById(id).orElse(null);
    if (instance == null) {
      throw new NotFoundException(UnitOfOrderableMessageKeys.ERROR_NOT_FOUND);
    }

    return getAuditLogResponse(UnitOfOrderable.class, id, author, changedPropertyName, page,
        returnJson);
  }

  /**
   * Get chosen unitOfOrderable.
   *
   * @param id UUID of unitOfOrderable which we want to get.
   * @return the UnitOfOrderable.
   */
  @GetMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public UnitOfOrderableDto getUnitOfOrderable(@PathVariable UUID id) {
    UnitOfOrderable unitOfOrderable = unitOfOrderableRepository.findById(id).orElse(null);
    if (unitOfOrderable == null) {
      throw new NotFoundException(UnitOfOrderableMessageKeys.ERROR_NOT_FOUND);
    } else {
      return exportToDto(unitOfOrderable);
    }
  }

  /**
   * Allows deleting unitOfOrderable.
   *
   * @param id UUID of unitOfOrderable which we want to delete
   */
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUnitOfOrderable(@PathVariable UUID id) {
    rightService.checkAdminRight(UNIT_OF_ORDERABLES_MANAGE);

    UnitOfOrderable unitOfOrderable = unitOfOrderableRepository.findById(id).orElse(null);
    if (unitOfOrderable == null) {
      throw new NotFoundException(UnitOfOrderableMessageKeys.ERROR_NOT_FOUND);
    } else {
      unitOfOrderableRepository.delete(unitOfOrderable);
    }
  }


  private UnitOfOrderableDto exportToDto(UnitOfOrderable unitOfOrderable) {

    UnitOfOrderableDto unitOfOrderableDto = new UnitOfOrderableDto();
    unitOfOrderable.export(unitOfOrderableDto);
    return unitOfOrderableDto;
  }

}
