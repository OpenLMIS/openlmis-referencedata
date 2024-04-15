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

import static org.openlmis.referencedata.domain.RightName.WARDS_MANAGE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Ward;
import org.openlmis.referencedata.dto.WardDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.WardRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.WardMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

@Controller
@RequestMapping(WardController.RESOURCE_PATH)
@Transactional
public class WardController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WardController.class);

  public static final String RESOURCE_PATH = API_PATH + "/wards";

  @Autowired
  private WardRepository wardRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Allows the creation of a new ward. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public WardDto createWard(@RequestBody WardDto ward) {
    rightService.checkAdminRight(WARDS_MANAGE);
    LOGGER.debug("Creating new ward");
    Ward newWard = Ward.newWard(ward);

    LOGGER.debug("Find facility");
    Facility facility = findFacility(ward.getFacility().getId());
    newWard.setFacility(facility);

    newWard.setId(null);
    newWard = wardRepository.saveAndFlush(newWard);

    return WardDto.newInstance(newWard);
  }

  /**
   * Updates the specified ward.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public WardDto saveWard(@PathVariable("id") UUID id,
      @RequestBody WardDto ward) {
    rightService.checkAdminRight(WARDS_MANAGE);
    if (null != ward.getId() && !Objects.equals(ward.getId(), id)) {
      throw new ValidationMessageException(WardMessageKeys.ERROR_WARD_ID_MISMATCH);
    }

    LOGGER.debug("Updating ward");
    Ward db;
    Optional<Ward> wardOptional = wardRepository.findById(id);
    if (wardOptional.isPresent()) {
      db = wardOptional.get();
      db.updateFrom(ward);
    } else {
      db = Ward.newWard(ward);
      db.setId(id);
    }

    LOGGER.debug("Find facility");
    Facility facility = findFacility(ward.getFacility().getId());
    db.setFacility(facility);

    wardRepository.saveAndFlush(db);

    return WardDto.newInstance(db);
  }

  /**
   * Deletes the specified ward.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWard(@PathVariable("id") UUID id) {
    rightService.checkAdminRight(WARDS_MANAGE);
    if (!wardRepository.existsById(id)) {
      throw new NotFoundException(WardMessageKeys.ERROR_WARD_NOT_FOUND);
    }

    wardRepository.deleteById(id);
  }

  /**
   * Retrieves all wards. Note that an empty collection rather than a 404 should be
   * returned if no wards exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<WardDto> getAllWards(
      @RequestParam(value = "facilityId", required = false) UUID facilityId,
      @RequestParam(value = "disabled", required = false) Boolean disabled,
      Pageable pageable) {
    Page<Ward> page =  wardRepository.search(facilityId, disabled, pageable);
    List<WardDto> content = page
        .getContent()
        .stream()
        .map(WardDto::newInstance)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified ward.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public WardDto getSpecifiedWard(@PathVariable("id") UUID id) {
    Ward ward = wardRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(WardMessageKeys.ERROR_WARD_NOT_FOUND));

    return WardDto.newInstance(ward);
  }

  /**
   * Retrieves audit information related to the specified ward.
   *
   * @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @GetMapping(value = "/{id}/auditLog")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getWardAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
      String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
      boolean returnJson,
      Pageable page) {
    rightService.checkAdminRight(WARDS_MANAGE);

    //Return a 404 if the specified instance can't be found
    Ward instance = wardRepository.findById(id).orElse(null);
    if (instance == null) {
      throw new NotFoundException(WardMessageKeys.ERROR_WARD_NOT_FOUND);
    }

    return getAuditLogResponse(Ward.class, id, author, changedPropertyName, page,
        returnJson);
  }

  private Facility findFacility(UUID facilityId) {
    return facilityRepository.findById(facilityId)
        .orElseThrow(() -> new ValidationMessageException(
            new Message(FacilityMessageKeys.ERROR_NOT_FOUND_WITH_ID, facilityId)));
  }

}
