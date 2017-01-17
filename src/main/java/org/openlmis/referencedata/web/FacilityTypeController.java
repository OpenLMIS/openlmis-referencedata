package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.exception.IntegrityViolationException;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
public class FacilityTypeController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityTypeController.class);

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  /**
   * Allows creating new facilityType. If the id is specified, it will be ignored.
   *
   * @param facilityType A facilityType bound to the request body
   * @return ResponseEntity containing the created facilityType
   */
  @RequestMapping(value = "/facilityTypes", method = RequestMethod.POST)
  public ResponseEntity<FacilityType> createFacilityType(@RequestBody FacilityType facilityType) {
    LOGGER.debug("Creating new facility type");
    facilityType.setId(null);
    facilityTypeRepository.save(facilityType);
    LOGGER.debug("Creating new facility type with id: " + facilityType.getId());
    return new ResponseEntity<>(facilityType, HttpStatus.CREATED);
  }

  /**
   * Get all facilityTypes.
   *
   * @return FacilityTypes.
   */
  @RequestMapping(value = "/facilityTypes", method = RequestMethod.GET)
  public ResponseEntity<Iterable<FacilityType>> getAllFacilityTypes() {
    Iterable<FacilityType> facilityTypes = facilityTypeRepository.findAll();
    return new ResponseEntity<>(facilityTypes, HttpStatus.OK);
  }

  /**
   * Allows updating facilityTypes.
   *
   * @param facilityType   A facilityType bound to the request body
   * @param facilityTypeId UUID of facilityType which we want to update
   * @return ResponseEntity containing the updated facilityType
   */
  @RequestMapping(value = "/facilityTypes/{id}", method = RequestMethod.PUT)
  public ResponseEntity<FacilityType> updateFacilityType(
      @RequestBody FacilityType facilityType, @PathVariable("id") UUID facilityTypeId) {

    FacilityType facilityTypeToUpdate = facilityTypeRepository.findOne(facilityTypeId);
    try {
      if (facilityTypeToUpdate == null) {
        facilityTypeToUpdate = new FacilityType();
        LOGGER.debug("Creating new facility type");
      } else {
        LOGGER.debug("Updating facility type with id: " + facilityTypeId);
      }

      facilityTypeToUpdate.updateFrom(facilityType);
      facilityTypeRepository.save(facilityTypeToUpdate);

      LOGGER.debug("Updating facility type with id: " + facilityTypeToUpdate.getId());
      return new ResponseEntity<>(facilityTypeToUpdate, HttpStatus.OK);
    } catch (DataIntegrityViolationException ex) {
      throw new IntegrityViolationException(FacilityTypeMessageKeys.ERROR_SAVING, ex);
    }
  }

  /**
   * Get chosen facilityType.
   *
   * @param facilityTypeId UUID of facilityType which we want to get
   * @return FacilityType.
   */
  @RequestMapping(value = "/facilityTypes/{id}", method = RequestMethod.GET)
  public ResponseEntity<FacilityType> getFacilityType(@PathVariable("id") UUID facilityTypeId) {
    FacilityType facilityType = facilityTypeRepository.findOne(facilityTypeId);
    if (facilityType == null) {
      throw new NotFoundException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityType, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting facilityType.
   *
   * @param facilityTypeId UUID of facilityType which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilityTypes/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteFacilityType(@PathVariable("id") UUID facilityTypeId) {
    FacilityType facilityType = facilityTypeRepository.findOne(facilityTypeId);
    if (facilityType == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        facilityTypeRepository.delete(facilityType);
      } catch (DataIntegrityViolationException ex) {
        throw new IntegrityViolationException(FacilityTypeMessageKeys.ERROR_DELETING, ex);
      }

      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }
}
