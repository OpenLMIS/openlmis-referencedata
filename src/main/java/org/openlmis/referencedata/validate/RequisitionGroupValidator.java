package org.openlmis.referencedata.validate;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A validator for {@link RequisitionGroup} object.
 */
@Component
public class RequisitionGroupValidator implements Validator {

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
   * @return true if {@code clazz} is equal to {@link RequisitionGroup} class definition. Otherwise
   *         false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return RequisitionGroup.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of {@link RequisitionGroup}
   * class.
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
    verifyProperties(errors);

    if (!errors.hasErrors()) {
      RequisitionGroup group = (RequisitionGroup) target;

      verifyCode(group.getCode(), errors);
      verifySupervisoryNode(group.getSupervisoryNode(), errors);
      verifyFacilities(group.getMemberFacilities(), errors);
    }
  }

  private void verifyArguments(Object target, Errors errors) {
    checkNotNull(target, "The Requisition Group cannot be null");
    checkNotNull(errors, "The contextual state about the validation process cannot be null");
  }

  private void verifyProperties(Errors errors) {
    // the Requisition Group Code is required
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors, "code", "code.empty", "The Requisition Group Code is required"
    );

    // the requisition group name is required
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors, "name", "name.empty", "The requisition Group Name is required"
    );

    // the supervisory node is required
    ValidationUtils.rejectIfEmpty(
        errors, "supervisoryNode", "supervisoryNode.empty", "The Supervisory Node is required"
    );
  }

  private void verifyCode(String code, Errors errors) {
    // requisition group code cannot be duplicated
    if (null != requisitionGroups.findByCode(code)) {
      errors.rejectValue(
          "code", "code.duplicate", "The Requisition Group Code cannot be duplicated"
      );
    }
  }

  private void verifySupervisoryNode(SupervisoryNode supervisoryNode, Errors errors) {
    // supervisory node matches a defined supervisory node
    if (null == supervisoryNode.getId()) {
      errors.rejectValue(
          "supervisoryNode", "supervisoryNode.missing.id",
          "The Supervisory Node must have ID"
      );
    } else if (null == supervisoryNodes.findOne(supervisoryNode.getId())) {
      errors.rejectValue(
          "supervisoryNode", "supervisoryNode.not.exist",
          "The Supervisory Node should match a defined supervisory node"
      );
    }
  }

  private void verifyFacilities(List<Facility> memberFacilities, Errors errors) {
    // facilities must already exist in the system (cannot add new facilities from this point)
    for (int i = 0, size = memberFacilities.size(); i < size; ++i) {
      Facility facility = memberFacilities.get(i);
      String field = "memberFacilities[" + i + "]";

      if (null == facility) {
        errors.rejectValue(
            field, "facility.is.null", "The facility can not be null"
        );
      } else if (null == facility.getId()) {
        errors.rejectValue(
            field, "facility.missing.id", "The facility must have ID"
        );
      } else if (null == facilities.findOne(facility.getId())) {
        errors.rejectValue(
            field, "facility.not.exist", "The facility should match a defined facility"
        );
      }
    }
  }

}
