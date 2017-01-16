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
public class RequisitionGroupValidator implements Validator {

  // prefix
  private static final String ERROR_CODE_PREFIX = "referencedata.requisitiongroup";

  // suffixes
  private static final String EMPTY = "empty";
  private static final String DUPLICATE = "duplicate";
  private static final String MISSING_ID = "missing.id";
  private static final String NOT_EXIST = "not.exist";
  private static final String IS_NULL = "is.null";
  private static final String IS_TOO_LONG = "is.too.long";

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
    Message targetMessage = new Message("referenceData.error.requisitionGroup.null");
    Message errorsMessage = new Message("referenceData.error.validation.contextualState.null");
    checkNotNull(target, targetMessage.toString());
    checkNotNull(errors, errorsMessage.toString());
  }

  private void verifyProperties(RequisitionGroupBaseDto group, Errors errors) {
    // the Requisition Group Code is required, length 50 characters
    rejectIfEmptyOrWhitespace(errors, CODE, EMPTY,
        "referenceData.error.requisitionGroup.code.required");

    // the requisition group name is required, length 50 characters
    rejectIfEmptyOrWhitespace(errors, NAME, EMPTY,
        "referenceData.error.requisitionGroup.name.required");

    // the supervisory node is required
    rejectIfEmpty(errors, SUPERVISORY_NODE, EMPTY,
        "referenceData.error.requisitionGroup.supervisoryNode.required");

    if (!errors.hasErrors()) {
      // the Requisition Group Code max length 50 characters
      if (group.getCode().length() > 50) {
        rejectValue(errors, CODE, IS_TOO_LONG, "referenceData.error.requisitionGroup.code.tooLong");
      }

      // the requisition group name max length 50 characters
      if (group.getName().length() > 50) {
        rejectValue(errors, NAME, IS_TOO_LONG, "referenceData.error.requisitionGroup.name.tooLong");
      }

      // description max length 250 characters
      if (null != group.getDescription() && group.getDescription().length() > 250) {
        rejectValue(errors, DESCRIPTION, IS_TOO_LONG,
            "referenceData.error.requisitionGroup.description.tooLong");
      }
    }
  }

  private void verifyCode(UUID id, String code, Errors errors) {
    // requisition group code cannot be duplicated
    RequisitionGroup db = requisitionGroups.findByCode(code);

    if (null != db && (null == id || !id.equals(db.getId()))) {
      rejectValue(errors, CODE, DUPLICATE, "referenceData.error.requisitionGroup.code.duplicated");
    }
  }

  private void verifySupervisoryNode(SupervisoryNodeBaseDto supervisoryNode, Errors errors) {
    // supervisory node matches a defined supervisory node
    if (null == supervisoryNode.getId()) {
      rejectValue(errors, SUPERVISORY_NODE, MISSING_ID,
          "referenceData.error.requisitionGroup.supervisoryNode.id.required");
    } else if (null == supervisoryNodes.findOne(supervisoryNode.getId())) {
      rejectValue(errors, SUPERVISORY_NODE, NOT_EXIST,
          "referenceData.error.requisitionGroup.supervisoryNode.nonExistent");
    }
  }

  private void verifyFacilities(List<FacilityDto> memberFacilities, Errors errors) {
    // facilities must already exist in the system (cannot add new facilities from this point)
    for (FacilityDto facility : memberFacilities) {
      if (null == facility) {
        rejectValue(errors, MEMBER_FACILITIES, IS_NULL,
            "referenceData.error.requisitionGroup.facility.null");
      } else if (null == facility.getId()) {
        rejectValue(errors, MEMBER_FACILITIES, MISSING_ID,
            "referenceData.error.requisitionGroup.facility.id.required");
      } else if (null == this.facilities.findOne(facility.getId())) {
        rejectValue(errors, MEMBER_FACILITIES, NOT_EXIST,
            "referenceData.error.requisitionGroup.facility.nonExistent");
      }
    }
  }

  private void rejectIfEmpty( Errors errors, String field, String suffix, String message) {
    ValidationUtils.rejectIfEmpty(errors, field, getErrorCode(field, suffix), message);
  }

  private void rejectIfEmptyOrWhitespace(
      Errors errors, String field, String suffix, String message) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, getErrorCode(field, suffix), message);
  }

  private void rejectValue(Errors errors, String field, String suffix, String message) {
    errors.rejectValue(field, getErrorCode(field, suffix), message);
  }

  private String getErrorCode(String field, String suffix) {
    return ERROR_CODE_PREFIX + '.' + field + '.' + suffix;
  }

}
