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

import static org.openlmis.referencedata.domain.RightName.FACILITY_APPROVED_PRODUCT_MANAGE;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys;
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

@Controller
@Transactional
public class FacilityTypeApprovedProductController extends BaseController {

  private static final Logger LOGGER =
        LoggerFactory.getLogger(FacilityTypeApprovedProductController.class);

  @Autowired
  private FacilityTypeApprovedProductRepository repository;

  /**
   * Allows creating new facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct A facilityTypeApprovedProduct bound to the request body.
   * @return the created facilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public FacilityTypeApprovedProduct createFacilityTypeApprovedProduct(
        @RequestBody FacilityTypeApprovedProduct facilityTypeApprovedProduct) {
    rightService.checkAdminRight(FACILITY_APPROVED_PRODUCT_MANAGE);
    LOGGER.debug("Creating new facilityTypeApprovedProduct");
    // Ignore provided id
    facilityTypeApprovedProduct.setId(null);
    repository.save(facilityTypeApprovedProduct);
    return facilityTypeApprovedProduct;
  }

  /**
   * Allows updating facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct A facilityTypeApprovedProduct bound to the request body.
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to update.
   * @return the updated facilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityTypeApprovedProduct updateFacilityTypeApprovedProduct(
        @RequestBody FacilityTypeApprovedProduct facilityTypeApprovedProduct,
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    rightService.checkAdminRight(FACILITY_APPROVED_PRODUCT_MANAGE);
    LOGGER.debug("Updating facilityTypeApprovedProduct");
    repository.save(facilityTypeApprovedProduct);
    return facilityTypeApprovedProduct;
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
  public FacilityTypeApprovedProduct getFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    rightService.checkAdminRight(FACILITY_APPROVED_PRODUCT_MANAGE);
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    } else {
      return facilityTypeApprovedProduct;
    }
  }

  /**
   * Allows deleting facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to delete.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  public void deleteFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    rightService.checkAdminRight(FACILITY_APPROVED_PRODUCT_MANAGE);
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    } else {
      repository.delete(facilityTypeApprovedProduct);
    }
  }
}
