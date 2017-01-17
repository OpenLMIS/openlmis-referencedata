package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityOperatorMessageKeys;
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
public class FacilityOperatorController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityOperatorController.class);

  @Autowired
  private FacilityOperatorRepository facilityOperatorRepository;

  /**
   * Allows creating new facilityOperators.
   *
   * @param facilityOperator A facilityOperator bound to the request body.
   * @return ResponseEntity containing the created facilityOperator.
   */
  @RequestMapping(value = "/facilityOperators", method = RequestMethod.POST)
  public ResponseEntity<FacilityOperator> createFacilityOperator(
      @RequestBody FacilityOperator facilityOperator) {
    LOGGER.debug("Creating new facility operator");
    // Ignore provided id
    facilityOperator.setId(null);
    facilityOperatorRepository.save(facilityOperator);
    return new ResponseEntity<>(facilityOperator, HttpStatus.CREATED);
  }

  /**
   * Get all facilityOperators.
   *
   * @return facilityOperators.
   */
  @RequestMapping(value = "/facilityOperators", method = RequestMethod.GET)
  public ResponseEntity<Iterable<FacilityOperator>> getAllFacilityOperators() {
    Iterable<FacilityOperator> facilityOperators = facilityOperatorRepository.findAll();
    if (facilityOperators == null) {
      throw new NotFoundException(FacilityOperatorMessageKeys.ERROR_NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityOperators, HttpStatus.OK);
    }
  }

  /**
   * Allows updating facilityOperator.
   *
   * @param facilityOperator   A facilityOperator bound to the request body
   * @param facilityOperatorId UUID of facilityOperator which we want to update
   * @return ResponseEntity containing the updated facilityOperator
   */
  @RequestMapping(value = "/facilityOperators/{id}", method = RequestMethod.PUT)
  public ResponseEntity<FacilityOperator> updateFacilityOperator(
      @RequestBody FacilityOperator facilityOperator, @PathVariable("id") UUID facilityOperatorId) {
    LOGGER.debug("Updating facility operator");
    facilityOperatorRepository.save(facilityOperator);
    return new ResponseEntity<>(facilityOperator, HttpStatus.OK);
  }

  /**
   * Get chosen facilityOperator.
   *
   * @param facilityOperatorId UUID of facilityOperator whose we want to get
   * @return facilityOperator.
   */
  @RequestMapping(value = "/facilityOperators/{id}", method = RequestMethod.GET)
  public ResponseEntity<FacilityOperator> getFacilityOperators(
      @PathVariable("id") UUID facilityOperatorId) {
    FacilityOperator facilityOperator = facilityOperatorRepository.findOne(facilityOperatorId);
    if (facilityOperator == null) {
      throw new NotFoundException(FacilityOperatorMessageKeys.ERROR_NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityOperator, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting facilityOperator.
   *
   * @param facilityOperatorId UUID of facilityOperator whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilityOperators/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteFacilityOperators(@PathVariable("id") UUID facilityOperatorId) {
    FacilityOperator facilityOperator = facilityOperatorRepository.findOne(facilityOperatorId);
    if (facilityOperator == null) {
      throw new NotFoundException(FacilityOperatorMessageKeys.ERROR_NOT_FOUND);
    } else {
      facilityOperatorRepository.delete(facilityOperator);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }
}
