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

import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.service.RequisitionGroupService;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.RequisitionGroupMessageKeys;
import org.openlmis.referencedata.validate.RequisitionGroupValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.openlmis.referencedata.domain.RightName.REQUISITION_GROUPS_MANAGE;

@Controller
@Transactional
public class RequisitionGroupController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequisitionGroupController.class);

  @Autowired
  @Qualifier("requisitionGroupValidator")
  private RequisitionGroupValidator validator;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  @Autowired
  private RequisitionGroupService requisitionGroupService;

  @Autowired
  private RightService rightService;

  /**
   * Allows creating new requisition group. If the id is specified, it will be ignored.
   *
   * @param requisitionGroupDto a requisition group bound to the request body.
   * @return the created RequisitionGroupDto.
   */
  @RequestMapping(value = "/requisitionGroups", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public RequisitionGroupDto createRequisitionGroup(
      @RequestBody RequisitionGroupDto requisitionGroupDto, BindingResult bindingResult) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    LOGGER.debug("Creating new requisitionGroup");
    validator.validate(requisitionGroupDto, bindingResult);

    if (bindingResult.getErrorCount() == 0) {
      requisitionGroupDto.setId(null);
      RequisitionGroup requisitionGroup = RequisitionGroup.newRequisitionGroup(requisitionGroupDto);
      requisitionGroupRepository.save(requisitionGroup);

      LOGGER.debug("Created new requisitionGroup with id: " + requisitionGroup.getId());
      return exportToDto(requisitionGroup);
    } else {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }
  }

  /**
   * Get all requisition groups.
   *
   * @return the RequisitionGroupDtos.
   */
  @RequestMapping(value = "/requisitionGroups", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<RequisitionGroupDto> getAllRequisitionGroups() {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);
    Iterable<RequisitionGroup> requisitionGroups = requisitionGroupRepository.findAll();
    List<RequisitionGroupDto> requisitionGroupDtos = new ArrayList<>();
    for (RequisitionGroup requisitionGroup : requisitionGroups) {
      requisitionGroupDtos.add(exportToDto(requisitionGroup));
    }
    return requisitionGroupDtos;
  }

  /**
   * Get chosen requisition group.
   *
   * @param requisitionGroupId the UUID of requisition group whose we want to get.
   * @return the RequisitionGroupDto.
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RequisitionGroupDto getRequisitionGroup(
      @PathVariable("id") UUID requisitionGroupId) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);

    RequisitionGroup requisitionGroup = requisitionGroupRepository.findOne(requisitionGroupId);
    if (requisitionGroup == null) {
      throw new NotFoundException(RequisitionGroupMessageKeys.ERROR_NOT_FOUND);
    } else {
      return exportToDto(requisitionGroup);
    }
  }

  /**
   * Allows updating requisition group.
   *
   * @param requisitionGroupDto A requisition group bound to the request body.
   * @param requisitionGroupId UUID of requisition group which we want to update.
   * @return the updated RequisitionGroupDto.
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RequisitionGroupDto updateRequisitionGroup(
      @RequestBody RequisitionGroupDto requisitionGroupDto,
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
      return exportToDto(requisitionGroupToUpdate);
    } else {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }
  }

  /**
   * Allows deleting requisition group.
   *
   * @param requisitionGroupId UUID of requisition group whose we want to delete.
   */
  @RequestMapping(value = "/requisitionGroups/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRequisitionGroup(@PathVariable("id") UUID requisitionGroupId) {
    rightService.checkAdminRight(REQUISITION_GROUPS_MANAGE);
    RequisitionGroup requisitionGroup = requisitionGroupRepository.findOne(requisitionGroupId);
    if (requisitionGroup == null) {
      throw new NotFoundException(RequisitionGroupMessageKeys.ERROR_NOT_FOUND);
    } else {
      requisitionGroupRepository.delete(requisitionGroup);
    }
  }

  /**
   * Retrieves required page of Requisition Groups that are matching given parameters.
   *
   * @param queryParams request parameters (code, name, zone, program).
   * @param pageable object used to encapsulate the pagination related values: page and size.
   * @return Page of wanted Requisition Groups matching query parameters.
   */
  @RequestMapping(value = "/requisitionGroups/search", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<RequisitionGroupDto> search(@RequestBody Map<String, Object> queryParams,
                                                    Pageable pageable) {
    rightService.checkAdminRight(RightName.REQUISITION_GROUPS_MANAGE);

    Page<RequisitionGroup> page = requisitionGroupService
        .searchRequisitionGroups(queryParams, pageable);

    return exportToDto(page, pageable);
  }

  private RequisitionGroupDto exportToDto(RequisitionGroup requisitionGroup) {
    RequisitionGroupDto requisitionGroupDto = null;

    if (requisitionGroup != null) {
      requisitionGroupDto = new RequisitionGroupDto();
      requisitionGroup.export(requisitionGroupDto);
    }

    return requisitionGroupDto;
  }

  private Page<RequisitionGroupDto> exportToDto(Page<RequisitionGroup> page, Pageable pageable) {
    List<RequisitionGroupDto> list = page.getContent().stream()
        .map(this::exportToDto).collect(Collectors.toList());
    return Pagination.getPage(list, pageable, page.getTotalElements());
  }
}
