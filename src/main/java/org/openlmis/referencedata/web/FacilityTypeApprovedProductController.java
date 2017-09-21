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

import static org.openlmis.referencedata.domain.RightName.FACILITY_APPROVED_ORDERABLES_MANAGE;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.ApprovedProductDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.service.FacilityTypeApprovedProductService;
import org.openlmis.referencedata.util.OrderableBuilder;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@Transactional
public class FacilityTypeApprovedProductController extends BaseController {

  private static final Logger LOGGER =
        LoggerFactory.getLogger(FacilityTypeApprovedProductController.class);

  @Autowired
  private FacilityTypeApprovedProductRepository repository;

  @Autowired
  private FacilityTypeApprovedProductService approvedProductService;

  @Autowired
  private OrderableBuilder orderableBuilder;

  /**
   * Allows creating new facilityTypeApprovedProduct.
   *
   * @param approvedProductDto A facilityTypeApprovedProduct bound to the request body.
   * @return the created facilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ApprovedProductDto createFacilityTypeApprovedProduct(
        @RequestBody ApprovedProductDto approvedProductDto) {
    rightService.checkAdminRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    validateFtapNotDuplicated(approvedProductDto);

    LOGGER.debug("Creating new facilityTypeApprovedProduct");
    Orderable orderable = orderableBuilder.newOrderable(approvedProductDto.getOrderable());

    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
        FacilityTypeApprovedProduct.newFacilityTypeApprovedProduct(approvedProductDto);
    facilityTypeApprovedProduct.setOrderable(orderable);

    // Ignore provided id
    facilityTypeApprovedProduct.setId(null);
    FacilityTypeApprovedProduct save = repository.save(facilityTypeApprovedProduct);
    return toDto(save);
  }

  /**
   * Allows updating facilityTypeApprovedProduct.
   *
   * @param approvedProductDto A facilityTypeApprovedProduct bound to the request body.
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to update.
   * @return the updated facilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ApprovedProductDto updateFacilityTypeApprovedProduct(
        @RequestBody ApprovedProductDto approvedProductDto,
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    rightService.checkAdminRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    validateFtapNotDuplicated(approvedProductDto);

    LOGGER.debug("Updating facilityTypeApprovedProduct");
    Orderable orderable = orderableBuilder.newOrderable(approvedProductDto.getOrderable());

    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
        FacilityTypeApprovedProduct.newFacilityTypeApprovedProduct(approvedProductDto);
    facilityTypeApprovedProduct.setOrderable(orderable);

    FacilityTypeApprovedProduct save = repository.save(facilityTypeApprovedProduct);
    return toDto(save);
  }

  /**
   * Get chosen facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct which we want to get
   * @return the FacilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ApprovedProductDto getFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    rightService.checkAdminRight(FACILITY_APPROVED_ORDERABLES_MANAGE);
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    } else {
      return toDto(facilityTypeApprovedProduct);
    }
  }

  /**
   * Search approved products by search criteria.
   *
   * @param queryParams a map containing search parameters. Supported keys are:
   *                    * facilityType [required]
   *                    * program
   * @return a list of approved products matching the criteria
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/search", method = RequestMethod.POST)
  @ResponseBody
  public List<ApprovedProductDto> searchFacilityTypeApprovedProducts(
        @RequestBody Map<String, String> queryParams) {
    rightService.checkAdminRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    Collection<FacilityTypeApprovedProduct> ftaps = approvedProductService.search(queryParams);
    return toDto(ftaps);
  }

  /**
   * Allows deleting facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to delete.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    rightService.checkAdminRight(FACILITY_APPROVED_ORDERABLES_MANAGE);
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    } else {
      repository.delete(facilityTypeApprovedProduct);
    }
  }


  /**
   * Get the audit information related to facility type approved products.
   *  @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getFacilityTypeApprovedProductAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      //Because JSON is all we formally support, returnJSON is excluded from our JavaDoc
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {

    rightService.checkAdminRight(FACILITY_APPROVED_ORDERABLES_MANAGE);

    //Return a 404 if the specified instance can't be found
    FacilityTypeApprovedProduct instance = repository.findOne(id);
    if (instance == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    }

    return getAuditLogResponse(
        FacilityTypeApprovedProduct.class, id, author, changedPropertyName, page, returnJson
    );
  }

  private ApprovedProductDto toDto(FacilityTypeApprovedProduct prod) {
    ApprovedProductDto productDto = new ApprovedProductDto();
    prod.export(productDto);
    return productDto;
  }

  private List<ApprovedProductDto> toDto(Collection<FacilityTypeApprovedProduct> prods) {
    List<ApprovedProductDto> dtos = new ArrayList<>();
    for (FacilityTypeApprovedProduct ftap : prods) {
      dtos.add(toDto(ftap));
    }

    return dtos;
  }

  private void validateFtapNotDuplicated(ApprovedProductDto approvedProductDto) {
    FacilityTypeApprovedProduct existing = repository
        .findByFacilityTypeIdAndOrderableIdAndProgramId(
            approvedProductDto.getFacilityType().getId(),
            approvedProductDto.getOrderable().getId(),
            approvedProductDto.getProgram().getId());

    if (existing != null
        && !Objects.equals(existing.getId(), approvedProductDto.getId())) {
      throw new ValidationMessageException(
          FacilityTypeApprovedProductMessageKeys.ERROR_DUPLICATED);
    }
  }
}
