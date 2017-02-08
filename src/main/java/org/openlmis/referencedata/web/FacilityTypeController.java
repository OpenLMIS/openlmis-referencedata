package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.exception.IntegrityViolationException;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
public class FacilityTypeController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityTypeController.class);

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  /**
   * Allows creating new facilityType. If the id is specified, it will be ignored.
   *
   * @param facilityType A facilityType bound to the request body
   * @return the created facilityType
   */
  @RequestMapping(value = "/facilityTypes", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public FacilityType createFacilityType(@RequestBody FacilityType facilityType) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    LOGGER.debug("Creating new facility type");
    facilityType.setId(null);
    facilityTypeRepository.save(facilityType);
    LOGGER.debug("Creating new facility type with id: " + facilityType.getId());
    return facilityType;
  }

  /**
   * Get all facilityTypes.
   *
   * @return FacilityTypes.
   */
  @RequestMapping(value = "/facilityTypes", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<FacilityType> getAllFacilityTypes() {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    return facilityTypeRepository.findAll();
  }

  /**
   * Allows updating facilityTypes.
   *
   * @param facilityType   A facilityType bound to the request body
   * @param facilityTypeId UUID of facilityType which we want to update
   * @return the updated facilityType
   */
  @RequestMapping(value = "/facilityTypes/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityType updateFacilityType(
      @RequestBody FacilityType facilityType, @PathVariable("id") UUID facilityTypeId) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);

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
      return facilityTypeToUpdate;
    } catch (DataIntegrityViolationException ex) {
      throw new IntegrityViolationException(FacilityTypeMessageKeys.ERROR_SAVING_WITH_ID, ex);
    }
  }

  /**
   * Get chosen facilityType.
   *
   * @param facilityTypeId UUID of facilityType which we want to get
   * @return the FacilityType.
   */
  @RequestMapping(value = "/facilityTypes/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityType getFacilityType(@PathVariable("id") UUID facilityTypeId) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityType facilityType = facilityTypeRepository.findOne(facilityTypeId);
    if (facilityType == null) {
      throw new NotFoundException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
    } else {
      return facilityType;
    }
  }

  /**
   * Allows deleting facilityType.
   *
   * @param facilityTypeId UUID of facilityType which we want to delete
   */
  @RequestMapping(value = "/facilityTypes/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFacilityType(@PathVariable("id") UUID facilityTypeId) {
    rightService.checkAdminRight(RightName.FACILITIES_MANAGE_RIGHT);
    FacilityType facilityType = facilityTypeRepository.findOne(facilityTypeId);
    if (facilityType == null) {
      throw new NotFoundException(FacilityTypeMessageKeys.ERROR_NOT_FOUND);
    } else {
      try {
        facilityTypeRepository.delete(facilityType);
      } catch (DataIntegrityViolationException ex) {
        throw new IntegrityViolationException(FacilityTypeMessageKeys.ERROR_DELETING_WITH_ID, ex);
      }
    }
  }
}
