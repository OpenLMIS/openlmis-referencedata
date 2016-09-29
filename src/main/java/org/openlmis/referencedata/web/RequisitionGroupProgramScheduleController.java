package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
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

import java.util.UUID;

@Controller
public class RequisitionGroupProgramScheduleController extends BaseController {

  private static final Logger LOGGER =
        LoggerFactory.getLogger(RequisitionGroupProgramScheduleController.class);

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  /**
   * Allows creating new requisitionGroupProgramSchedule.
   * If the id is specified, it will be ignored.
   *
   * @param requisition A requisitionGroupProgramSchedule bound to the request body
   * @return ResponseEntity containing the created requisitionGroupProgramSchedule
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules", method = RequestMethod.POST)
  public ResponseEntity<?> createRequisitionGroupProgramSchedule(
        @RequestBody RequisitionGroupProgramSchedule requisition) {
    try {
      LOGGER.debug("Creating new requisitionGroupProgramSchedule");
      requisition.setId(null);
      RequisitionGroupProgramSchedule newRequisition = repository.save(requisition);
      LOGGER.debug("Created new requisitionGroupProgramSchedule with id: " + requisition.getId());
      return new ResponseEntity<RequisitionGroupProgramSchedule>(
            newRequisition, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while creating"
                  + "requisitionGroupProgramSchedule", ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get all requisitionGroupProgramSchedules.
   *
   * @return RequisitionGroupProgramSchedules.
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRequisitionGroupProgramSchedule() {
    Iterable<RequisitionGroupProgramSchedule> requisitions = repository.findAll();
    return new ResponseEntity<>(requisitions, HttpStatus.OK);
  }

  /**
   * Get chosen requisitionGroupProgramSchedule.
   *
   * @param requisitionId UUID of requisitionGroupProgramSchedule whose we want to get
   * @return RequisitionGroupProgramSchedule.
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getRequisitionGroupProgramSchedule(
        @PathVariable("id") UUID requisitionId) {
    RequisitionGroupProgramSchedule requisition = repository.findOne(requisitionId);
    if (requisition == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(requisition, HttpStatus.OK);
    }
  }

  //after last changes on referencedata its currently not working
  /**
   * Allows updating requisitionGroupProgramSchedule.
   *
   * @param reqGroupProgSchedule A requisitionGroupProgramSchedule
   *                                        bound to the request body
   * @param requisitionId UUID of requisitionGroupProgramSchedule
   *                                          which we want to update
   * @return ResponseEntity containing the updated requisitionGroup
   */
  /*
  @RequestMapping(value = "/requisitionGroupProgramSchedules/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRequisitionGroupProgramSchedule(
        @RequestBody RequisitionGroupProgramSchedule reqGroupProgSchedule,
        @PathVariable("id") UUID requisitionId) {

    RequisitionGroupProgramSchedule reqGroupProgScheduleToUpdate =
          repository.findOne(requisitionId);
    try {
      if (reqGroupProgScheduleToUpdate == null) {
        reqGroupProgScheduleToUpdate = new RequisitionGroupProgramSchedule();
        LOGGER.info("Creating new requisitionGroupProgramSchedule");
      } else {
        LOGGER.debug("Updating requisitionGPS with id: " + requisitionId);
      }

      reqGroupProgScheduleToUpdate.updateFrom(reqGroupProgSchedule);
      reqGroupProgScheduleToUpdate = repository.save(reqGroupProgScheduleToUpdate);

      LOGGER.debug("Saved requisitionGroupProgramSchedule with id: "
            + reqGroupProgScheduleToUpdate.getId());
      return new ResponseEntity<RequisitionGroupProgramSchedule>(
            reqGroupProgScheduleToUpdate, HttpStatus.OK);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while saving requisitionGroupProgramSchedule"
                  + " with id: " + reqGroupProgScheduleToUpdate.getId(), ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }*/

  /**
   * Allows deleting requisitionGroupProgramSchedule.
   *
   * @param requisitionId UUID of requisitionGroupProgramSchedule whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRequisitionGroupProgramSchedule(
        @PathVariable("id") UUID requisitionId) {
    RequisitionGroupProgramSchedule requisition = repository.findOne(requisitionId);
    if (requisition == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        repository.delete(requisition);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse =
              new ErrorResponse("An error accurred while deleting requisitionGroupProgramSchedule"
                    + " with id: " + requisitionId, ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<RequisitionGroupProgramSchedule>(HttpStatus.NO_CONTENT);
    }
  }
}
