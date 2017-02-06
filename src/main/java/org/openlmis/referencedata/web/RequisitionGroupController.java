package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.dto.RequisitionGroupBaseDto;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.util.messagekeys.RequisitionGroupMessageKeys;
import org.openlmis.referencedata.validate.RequisitionGroupValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.openlmis.referencedata.domain.RightName.REQUISITION_GROUPS_MANAGE;

@Controller
public class RequisitionGroupController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionGroupController.class);

  @Autowired
  @Qualifier("requisitionGroupValidator")
  private RequisitionGroupValidator validator;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  @Autowired
  private RightService rightService;

  /**
   * Allows creating new requisitionGroup. If the id is specified, it will be ignored.
   *
   * @param requisitionGroupDto A requisitionGroup bound to the request body
   * @return ResponseEntity containing the created requisitionGroupDto
   */
  @RequestMapping(value = "/requisitionGroups", method = RequestMethod.POST)
  public ResponseEntity<RequisitionGroupDto> createRequisitionGroup(
      @RequestBody RequisitionGroupBaseDto requisitionGroupDto, BindingResult bindingResult) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    LOGGER.debug("Creating new requisitionGroup");
    validator.validate(requisitionGroupDto, bindingResult);

    if (bindingResult.getErrorCount() == 0) {
      requisitionGroupDto.setId(null);
      RequisitionGroup requisitionGroup = RequisitionGroup.newRequisitionGroup(requisitionGroupDto);
      requisitionGroupRepository.save(requisitionGroup);

      LOGGER.debug("Created new requisitionGroup with id: " + requisitionGroup.getId());
      return new ResponseEntity<>(exportToDto(requisitionGroup), HttpStatus.CREATED);
    } else {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }
  }

  /**
   * Get all requisitionGroups.
   *
   * @return RequisitionGroupDtos.
   */
  @RequestMapping(value = "/requisitionGroups", method = RequestMethod.GET)
  public ResponseEntity<List<RequisitionGroupDto>> getAllRequisitionGroups() {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    Iterable<RequisitionGroup> requisitionGroups = requisitionGroupRepository.findAll();
    List<RequisitionGroupDto> requisitionGroupDtos = new ArrayList<>();
    for (RequisitionGroup requisitionGroup : requisitionGroups) {
      requisitionGroupDtos.add(exportToDto(requisitionGroup));
    }
    return new ResponseEntity<>(requisitionGroupDtos, HttpStatus.OK);
  }

  /**
   * Get chosen requisitionGroup.
   *
   * @param requisitionGroupId UUID of requisitionGroup whose we want to get
   * @return RequisitionGroupDto.
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.GET)
  public ResponseEntity<RequisitionGroupDto> getRequisitionGroup(
      @PathVariable("id") UUID requisitionGroupId) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    RequisitionGroup requisitionGroup = requisitionGroupRepository.findOne(requisitionGroupId);
    if (requisitionGroup == null) {
      throw new NotFoundException(RequisitionGroupMessageKeys.ERROR_NOT_FOUND);
    } else {
      return new ResponseEntity<>(exportToDto(requisitionGroup), HttpStatus.OK);
    }
  }

  /**
   * Allows updating requisitionGroup.
   *
   * @param requisitionGroupDto A requisitionGroupDto bound to the request body
   * @param requisitionGroupId UUID of requisitionGroup which we want to update
   * @return ResponseEntity containing the updated requisitionGroupDto
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.PUT)
  public ResponseEntity<RequisitionGroupDto> updateRequisitionGroup(
      @RequestBody RequisitionGroupBaseDto requisitionGroupDto,
      @PathVariable("id") UUID requisitionGroupId,
      BindingResult bindingResult) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    validator.validate(requisitionGroupDto, bindingResult);

    if (bindingResult.getErrorCount() == 0) {
      RequisitionGroup requisitionGroupToUpdate =
          requisitionGroupRepository.findOne(requisitionGroupId);

      if (null == requisitionGroupToUpdate) {
        LOGGER.info("Creating new requisitionGroup");
        requisitionGroupToUpdate = new RequisitionGroup();
      } else {
        LOGGER.debug("Updating requisitionGroup with id: " + requisitionGroupId);
      }

      requisitionGroupToUpdate.updateFrom(
          RequisitionGroup.newRequisitionGroup(requisitionGroupDto));
      requisitionGroupToUpdate = requisitionGroupRepository.save(requisitionGroupToUpdate);

      LOGGER.debug("Saved requisitionGroup with id: " + requisitionGroupToUpdate.getId());
      return new ResponseEntity<>(exportToDto(requisitionGroupToUpdate), HttpStatus.OK);
    } else {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }
  }

  /**
   * Allows deleting requisitionGroup.
   *
   * @param requisitionGroupId UUID of requisitionGroup whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteRequisitionGroup(@PathVariable("id") UUID requisitionGroupId) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    RequisitionGroup requisitionGroup = requisitionGroupRepository.findOne(requisitionGroupId);
    if (requisitionGroup == null) {
      throw new NotFoundException(RequisitionGroupMessageKeys.ERROR_NOT_FOUND);
    } else {
      requisitionGroupRepository.delete(requisitionGroup);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }

  private RequisitionGroupDto exportToDto(RequisitionGroup requisitionGroup) {
    RequisitionGroupDto requisitionGroupDto = null;

    if (requisitionGroup != null) {
      requisitionGroupDto = new RequisitionGroupDto();
      requisitionGroup.export(requisitionGroupDto);
    }

    return requisitionGroupDto;
  }
}
