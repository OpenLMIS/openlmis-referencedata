package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.util.ErrorResponse;
import org.openlmis.referencedata.validate.RequisitionGroupValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
public class RequisitionGroupController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionGroupController.class);

  @Autowired
  @Qualifier("requisitionGroupValidator")
  private RequisitionGroupValidator validator;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  /**
   * Allows creating new requisitionGroup.
   * If the id is specified, it will be ignored.
   *
   * @param requisitionGroup A requisitionGroup bound to the request body
   * @return ResponseEntity containing the created requisitionGroup
   */
  @RequestMapping(value = "/requisitionGroups", method = RequestMethod.POST)
  public ResponseEntity<?> createRequisitionGroup(@RequestBody RequisitionGroup requisitionGroup,
                                                  BindingResult bindingResult) {
    LOGGER.debug("Creating new requisitionGroup");
    validator.validate(requisitionGroup, bindingResult);

    if (bindingResult.getErrorCount() == 0) {
      requisitionGroup.setId(null);
      RequisitionGroup newRequisitionGroup = requisitionGroupRepository.save(requisitionGroup);

      LOGGER.debug("Created new requisitionGroup with id: " + requisitionGroup.getId());
      return new ResponseEntity<>(newRequisitionGroup, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Get all requisitionGroups.
   *
   * @return RequisitionGroups.
   */
  @RequestMapping(value = "/requisitionGroups", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRequisitionGroup() {
    Iterable<RequisitionGroup> requisitionGroups = requisitionGroupRepository.findAll();
    return new ResponseEntity<>(requisitionGroups, HttpStatus.OK);
  }

  /**
   * Get chosen requisitionGroup.
   *
   * @param requisitionGroupId UUID of requisitionGroup whose we want to get
   * @return RequisitionGroup.
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getRequisitionGroup(@PathVariable("id") UUID requisitionGroupId) {
    RequisitionGroup requisitionGroup = requisitionGroupRepository.findOne(requisitionGroupId);
    if (requisitionGroup == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(requisitionGroup, HttpStatus.OK);
    }
  }

  /**
   * Allows updating requisitionGroup.
   *
   * @param requisitionGroup   A requisitionGroup bound to the request body
   * @param requisitionGroupId UUID of requisitionGroup which we want to update
   * @return ResponseEntity containing the updated requisitionGroup
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRequisitionGroup(@RequestBody RequisitionGroup requisitionGroup,
                                                  @PathVariable("id") UUID requisitionGroupId,
                                                  BindingResult bindingResult) {
    validator.validate(requisitionGroup, bindingResult);

    if (bindingResult.getErrorCount() == 0) {
      RequisitionGroup requisitionGroupToUpdate =
          requisitionGroupRepository.findOne(requisitionGroupId);

      if (null == requisitionGroupToUpdate) {
        LOGGER.info("Creating new requisitionGroup");
        requisitionGroupToUpdate = new RequisitionGroup();
      } else {
        LOGGER.debug("Updating requisitionGroup with id: " + requisitionGroupId);
      }

      try {
        requisitionGroupToUpdate.updateFrom(requisitionGroup);
        requisitionGroupToUpdate = requisitionGroupRepository.save(requisitionGroupToUpdate);

        LOGGER.debug("Saved requisitionGroup with id: " + requisitionGroupToUpdate.getId());
        return new ResponseEntity<>(requisitionGroupToUpdate, HttpStatus.OK);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while saving requisitionGroup with id: "
                + requisitionGroupToUpdate.getId(), ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Allows deleting requisitionGroup.
   *
   * @param requisitionGroupId UUID of requisitionGroup whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRequisitionGroup(@PathVariable("id") UUID requisitionGroupId) {
    RequisitionGroup requisitionGroup = requisitionGroupRepository.findOne(requisitionGroupId);
    if (requisitionGroup == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        requisitionGroupRepository.delete(requisitionGroup);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse =
            new ErrorResponse("An error accurred while deleting requisitionGroup with id: "
                + requisitionGroupId, ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<RequisitionGroup>(HttpStatus.NO_CONTENT);
    }
  }
}
