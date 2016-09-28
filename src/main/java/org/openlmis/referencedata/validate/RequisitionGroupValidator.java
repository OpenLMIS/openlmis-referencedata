package org.openlmis.referencedata.validate;

import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

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

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is
   *        being asked if it can {@link #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link RequisitionGroup} class
   *         definition. Otherwise false.
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
    checkNotNull(target, "The Requisition Group cannot be null");
    checkNotNull(errors, "The contextual state about the validation process cannot be null");

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

    if (!errors.hasErrors()) {
      RequisitionGroup group = (RequisitionGroup) target;

      // requisition group code cannot be duplicated
      if (null != requisitionGroups.findByCode(group.getCode())) {
        errors.rejectValue(
            "code", "code.duplicate", "The Requisition Group Code cannot be duplicated"
        );
      }

      // supervisory node matches a defined supervisory node
      if (null == supervisoryNodes.findByCode(group.getSupervisoryNode().getCode())) {
        errors.rejectValue(
            "supervisoryNode", "supervisoryNode.not.exist",
            "The Supervisory Node should match a defined supervisory node"
        );
      }
    }
  }

}
