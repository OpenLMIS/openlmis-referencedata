package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.util.ErrorResponse;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class FacilityController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityController.class);

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Allows creating new facilities.
   * If the id is specified, it will be ignored.
   *
   * @param facility A facility bound to the request body
   * @return ResponseEntity containing the created facility
   */
  @RequestMapping(value = "/facilities", method = RequestMethod.POST)
  public ResponseEntity<?> createFacility(@RequestBody Facility facility) {
    try {
      LOGGER.debug("Creating new facility");
      facility.setId(null);
      Facility newFacility = facilityRepository.save(facility);
      LOGGER.debug("Created new facility with id: " + facility.getId());
      return new ResponseEntity<Facility>(newFacility, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while creating facility", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get all facilities.
   *
   * @return Facilities.
   */
  @RequestMapping(value = "/facilities", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> getAllFacilities() {
    Iterable<Facility> facilities = facilityRepository.findAll();
    return new ResponseEntity<>(facilities, HttpStatus.OK);
  }


  /**
   * Allows updating facilities.
   *
   * @param facility A facility bound to the request body
   * @param facilityId UUID of facility which we want to update
   * @return ResponseEntity containing the updated facility
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateFacilities(@RequestBody Facility facility,
                                       @PathVariable("id") UUID facilityId) {

    Facility facilityToUpdate = facilityRepository.findOne(facilityId);
    try {
      if (facilityToUpdate == null) {
        facilityToUpdate = new Facility();
        LOGGER.info("Creating new facility");
      } else {
        LOGGER.debug("Updating facility with id: " + facilityId);
      }
      
      facilityToUpdate = facilityRepository.save(facilityToUpdate);

      LOGGER.debug("Saved facility with id: " + facilityToUpdate.getId());
      return new ResponseEntity<Facility>(facilityToUpdate, HttpStatus.OK);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while saving facility with id: "
                  + facilityToUpdate.getId(), ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get chosen facility.
   *
   * @param facilityId UUID of facility which we want to get
   * @return Facility.
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getFacility(@PathVariable("id") UUID facilityId) {
    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(facility, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting facility.
   *
   * @param facilityId UUID of facility which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilities/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteFacility(@PathVariable("id") UUID facilityId) {
    Facility facility = facilityRepository.findOne(facilityId);
    if (facility == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        facilityRepository.delete(facility);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse =
              new ErrorResponse("An error accurred while deleting facility with id: "
                    + facilityId, ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<Facility>(HttpStatus.NO_CONTENT);
    }
  }
}
