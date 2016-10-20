package org.openlmis.referencedata.validate;

import static com.google.common.base.Preconditions.checkNotNull;

import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.RequisitionGroupBaseDto;
import org.openlmis.referencedata.dto.SupervisoryNodeBaseDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
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

  // code error messages
  static final String CODE_IS_REQUIRED = "The Requisition Group Code is required";
  static final String CODE_CANNOT_BE_DUPLICATED = "The Requisition Group Code cannot be duplicated";
  static final String CODE_IS_TOO_LONG = "The Requisition Group Code can have max 50 characters";

  // name error messages
  static final String NAME_IS_REQUIRED = "The requisition Group Name is required";
  static final String NAME_IS_TOO_LONG = "The Requisition Group Name can have max 50 characters";

  // description error messages
  static final String DESCRIPTION_IS_TOO_LONG =
      "The Requisition Group Description can have max 250 characters";

  // supervisory node error messages
  static final String SUPERVISORY_NODE_IS_REQUIRED = "The Supervisory Node is required";
  static final String SUPERVISORY_NODE_MUST_HAVE_ID = "The Supervisory Node must have ID";
  static final String SUPERVISORY_NODE_MUST_EXIST =
      "The Supervisory Node should match a defined supervisory node";

  // facility error messages
  static final String FACILITY_CAN_NOT_BE_NULL = "The facility can not be null";
  static final String FACILITY_MUST_HAVE_ID = "The facility must have ID";
  static final String FACILITY_MUST_EXIST = "The facility should match a defined facility";

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
          .orElse(Collections.emptyList()).stream().map(facility -> (FacilityDto) facility)
          .collect(Collectors.toList()), errors);
    }
  }

  private void verifyArguments(Object target, Errors errors) {
    checkNotNull(target, "The Requisition Group cannot be null");
    checkNotNull(errors, "The contextual state about the validation process cannot be null");
  }

  private void verifyProperties(RequisitionGroupBaseDto group, Errors errors) {
    // the Requisition Group Code is required, length 50 characters
    rejectIfEmptyOrWhitespace(errors, CODE, EMPTY, CODE_IS_REQUIRED);

    // the requisition group name is required, length 50 characters
    rejectIfEmptyOrWhitespace(errors, NAME, EMPTY, NAME_IS_REQUIRED);

    // the supervisory node is required
    rejectIfEmpty(errors, SUPERVISORY_NODE, EMPTY, SUPERVISORY_NODE_IS_REQUIRED);

    if (!errors.hasErrors()) {
      // the Requisition Group Code max length 50 characters
      if (group.getCode().length() > 50) {
        rejectValue(errors, CODE, IS_TOO_LONG, CODE_IS_TOO_LONG);
      }

      // the requisition group name max length 50 characters
      if (group.getName().length() > 50) {
        rejectValue(errors, NAME, IS_TOO_LONG, NAME_IS_TOO_LONG);
      }

      // description max length 250 characters
      if (null != group.getDescription() && group.getDescription().length() > 250) {
        rejectValue(errors, DESCRIPTION, IS_TOO_LONG, DESCRIPTION_IS_TOO_LONG);
      }
    }
  }

  private void verifyCode(UUID id, String code, Errors errors) {
    // requisition group code cannot be duplicated
    RequisitionGroup db = requisitionGroups.findByCode(code);

    if (null != db && (null == id || !id.equals(db.getId()))) {
      rejectValue(errors, CODE, DUPLICATE, CODE_CANNOT_BE_DUPLICATED);
    }
  }

  private void verifySupervisoryNode(SupervisoryNodeBaseDto supervisoryNode, Errors errors) {
    // supervisory node matches a defined supervisory node
    if (null == supervisoryNode.getId()) {
      rejectValue(errors, SUPERVISORY_NODE, MISSING_ID, SUPERVISORY_NODE_MUST_HAVE_ID);
    } else if (null == supervisoryNodes.findOne(supervisoryNode.getId())) {
      rejectValue(errors, SUPERVISORY_NODE, NOT_EXIST, SUPERVISORY_NODE_MUST_EXIST);
    }
  }

  private void verifyFacilities(List<FacilityDto> memberFacilities, Errors errors) {
    // facilities must already exist in the system (cannot add new facilities from this point)
    for (FacilityDto facility : memberFacilities) {
      if (null == facility) {
        rejectValue(errors, MEMBER_FACILITIES, IS_NULL, FACILITY_CAN_NOT_BE_NULL);
      } else if (null == facility.getId()) {
        rejectValue(errors, MEMBER_FACILITIES, MISSING_ID, FACILITY_MUST_HAVE_ID);
      } else if (null == this.facilities.findOne(facility.getId())) {
        rejectValue(errors, MEMBER_FACILITIES, NOT_EXIST, FACILITY_MUST_EXIST);
      }
    }
  }

  private void rejectIfEmpty(Errors errors, String field,
                             String suffix, String message) {
    ValidationUtils.rejectIfEmpty(
        errors, field, getErrorCode(field, suffix), message
    );
  }

  private void rejectIfEmptyOrWhitespace(Errors errors, String field,
                                         String suffix, String message) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors, field, getErrorCode(field, suffix), message
    );
  }

  private void rejectValue(Errors errors, String field, String suffix, String message) {
    errors.rejectValue(field, getErrorCode(field, suffix), message);
  }

  private String getErrorCode(String field, String suffix) {
    return ERROR_CODE_PREFIX + '.' + field + '.' + suffix;
  }

}
