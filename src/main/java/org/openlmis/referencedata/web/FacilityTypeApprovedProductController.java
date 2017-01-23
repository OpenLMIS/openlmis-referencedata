package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
public class FacilityTypeApprovedProductController extends BaseController {

  private static final Logger LOGGER =
        LoggerFactory.getLogger(FacilityTypeApprovedProductController.class);

  @Autowired
  private FacilityTypeApprovedProductRepository repository;

  /**
   * Allows creating new facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct A facilityTypeApprovedProduct bound to the request body
   * @return ResponseEntity containing the created facilityTypeApprovedProduct
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts", method = RequestMethod.POST)
  public ResponseEntity<FacilityTypeApprovedProduct> createFacilityTypeApprovedProduct(
        @RequestBody FacilityTypeApprovedProduct facilityTypeApprovedProduct) {
    LOGGER.debug("Creating new facilityTypeApprovedProduct");
    // Ignore provided id
    facilityTypeApprovedProduct.setId(null);
    repository.save(facilityTypeApprovedProduct);
    return new ResponseEntity<>(facilityTypeApprovedProduct, HttpStatus.CREATED);
  }

  /**
   * Allows updating facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct A facilityTypeApprovedProduct bound to the request body
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to update
   * @return ResponseEntity containing the updated facilityTypeApprovedProduct
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.PUT)
  public ResponseEntity<FacilityTypeApprovedProduct> updateFacilityTypeApprovedProduct(
        @RequestBody FacilityTypeApprovedProduct facilityTypeApprovedProduct,
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    LOGGER.debug("Updating facilityTypeApprovedProduct");
    repository.save(facilityTypeApprovedProduct);
    return new ResponseEntity<>(facilityTypeApprovedProduct, HttpStatus.OK);
  }

  /**
   * Get chosen facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct which we want to get
   * @return FacilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.GET)
  public ResponseEntity<FacilityTypeApprovedProduct> getFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityTypeApprovedProduct, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      throw new NotFoundException(FacilityTypeApprovedProductMessageKeys.ERROR_NOT_FOUND);
    } else {
      repository.delete(facilityTypeApprovedProduct);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }
}
