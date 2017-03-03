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

package org.openlmis.referencedata.validate;

import static com.google.common.base.Preconditions.checkNotNull;

import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.RequisitionGroupBaseDto;
import org.openlmis.referencedata.dto.SupervisoryNodeBaseDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.RequisitionGroupMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ValidationMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A validator for {@link RequisitionGroupBaseDto} object.
 */
@Component
public class RequisitionGroupValidator implements BaseValidator {

  // RequisitionGroup fields
  static final String CODE = "code";
  static final String NAME = "name";
  static final String DESCRIPTION = "description";
  static final String SUPERVISORY_NODE = "supervisoryNode";
  static final String MEMBER_FACILITIES = "memberFacilities";

  @Autowired
  private SupervisoryNodeRepository supervisoryNodes;

  @Autowired
  private RequisitionGroupRepository requisitionGroups;

  @Autowired
  private FacilityRepository facilities;

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link RequisitionGroupBaseDto} class definition.
   *     Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return RequisitionGroupBaseDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of
   * {@link RequisitionGroupBaseDto} class.
   * <p>Firstly, the method checks if the target object has a value in {@code code} and {@code name}
   * properties. For those two properties the value cannot be {@code null}, empty or
   * contains only whitespaces.</p>
   * <p>Secondly, the method checks if the target object has a non-null value in
   * {@code supervisoryNode}.</p>
   * <p>If there are no errors, the method checks if the {@code code} property in the target
   * object is unique and if the Supervisory Node exists in database.</p>
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @throws NullPointerException if any method parameter is null.
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors);

    RequisitionGroupBaseDto group = (RequisitionGroupBaseDto) target;

    verifyProperties(group, errors);

    if (!errors.hasErrors()) {
      verifyCode(group.getId(), group.getCode(), errors);
      verifySupervisoryNode(group.getSupervisoryNode(), errors);
      verifyFacilities(Optional.ofNullable(group.getMemberFacilities())
          .orElse(Collections.emptySet()).stream().map(facility -> (FacilityDto) facility)
          .collect(Collectors.toList()), errors);
    }
  }

  private void verifyArguments(Object target, Errors errors) {
    Message targetMessage = new Message(RequisitionGroupMessageKeys.ERROR_NULL);
    Message errorsMessage = new Message(ValidationMessageKeys.ERROR_CONTEXTUAL_STATE_NULL);
    checkNotNull(target, targetMessage.toString());
    checkNotNull(errors, errorsMessage.toString());
  }

  private void verifyProperties(RequisitionGroupBaseDto group, Errors errors) {
    // the Requisition Group Code is required, length 50 characters
    rejectIfEmptyOrWhitespace(errors, CODE, RequisitionGroupMessageKeys.ERROR_CODE_REQUIRED);

    // the requisition group name is required, length 50 characters
    rejectIfEmptyOrWhitespace(errors, NAME, RequisitionGroupMessageKeys.ERROR_NAME_REQUIRED);

    // the supervisory node is required
    rejectIfEmpty(errors, SUPERVISORY_NODE,
        RequisitionGroupMessageKeys.ERROR_SUPERVISORY_NODE_ID_REQUIRED);

    if (!errors.hasErrors()) {
      // the Requisition Group Code max length 50 characters
      if (group.getCode().length() > 50) {
        rejectValue(errors, CODE, RequisitionGroupMessageKeys.ERROR_CODE_TOO_LONG);
      }

      // the requisition group name max length 50 characters
      if (group.getName().length() > 50) {
        rejectValue(errors, NAME, RequisitionGroupMessageKeys.ERROR_NAME_TOO_LONG);
      }

      // description max length 250 characters
      if (null != group.getDescription() && group.getDescription().length() > 250) {
        rejectValue(errors, DESCRIPTION, RequisitionGroupMessageKeys.ERROR_DESCRIPTION_TOO_LONG);
      }
    }
  }

  private void verifyCode(UUID id, String code, Errors errors) {
    // requisition group code cannot be duplicated
    RequisitionGroup db = requisitionGroups.findByCode(code);

    if (null != db && (null == id || !id.equals(db.getId()))) {
      rejectValue(errors, CODE, RequisitionGroupMessageKeys.ERROR_CODE_DUPLICATED);
    }
  }

  private void verifySupervisoryNode(SupervisoryNodeBaseDto supervisoryNode, Errors errors) {
    // supervisory node matches a defined supervisory node
    if (null == supervisoryNode.getId()) {
      rejectValue(errors, SUPERVISORY_NODE,
          RequisitionGroupMessageKeys.ERROR_SUPERVISORY_NODE_ID_REQUIRED);
    } else if (null == supervisoryNodes.findOne(supervisoryNode.getId())) {
      rejectValue(errors, SUPERVISORY_NODE,
          RequisitionGroupMessageKeys.ERROR_SUPERVISORY_NODE_NON_EXISTENT);
    }
  }

  private void verifyFacilities(List<FacilityDto> memberFacilities, Errors errors) {
    // facilities must already exist in the system (cannot add new facilities from this point)
    for (FacilityDto facility : memberFacilities) {
      if (null == facility) {
        rejectValue(errors, MEMBER_FACILITIES, RequisitionGroupMessageKeys.ERROR_FACILITY_NULL);
      } else if (null == facility.getId()) {
        rejectValue(errors, MEMBER_FACILITIES,
            RequisitionGroupMessageKeys.ERROR_FACILITY_ID_REQUIRED);
      } else if (null == this.facilities.findOne(facility.getId())) {
        rejectValue(errors, MEMBER_FACILITIES,
            RequisitionGroupMessageKeys.ERROR_FACILITY_NON_EXISTENT);
      }
    }
  }
}
