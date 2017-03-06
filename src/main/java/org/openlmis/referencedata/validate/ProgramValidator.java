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

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ValidationMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.UUID;

/**
 * A validator for {@link org.openlmis.referencedata.dto.ProgramDto} object.
 */
@Component
public class ProgramValidator implements BaseValidator {

  // Program fields
  static final String CODE = "code";

  @Autowired
  private ProgramRepository programRepository;

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link ProgramDto} class definition.
   *     Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return ProgramDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of
   * {@link ProgramDto} class.
   * <p>Firstly, the method checks if the target object has a value in {@code code} and {@code name}
   * properties. For those two properties the value cannot be {@code null}, empty or
   * contains only whitespaces.</p>
   * <p>If there are no errors, the method checks if the {@code code} property in the target
   * object is unique.</p>
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors);

    ProgramDto program = (ProgramDto) target;

    verifyProperties(errors);

    if (!errors.hasErrors()) {
      verifyCode(program.getId(), Code.code(program.getCode()), errors);
    }
  }

  private void verifyArguments(Object target, Errors errors) {
    Message targetMessage = new Message(ProgramMessageKeys.ERROR_NULL);
    Message errorsMessage = new Message(ValidationMessageKeys.ERROR_CONTEXTUAL_STATE_NULL);
    checkNotNull(target, targetMessage.toString());
    checkNotNull(errors, errorsMessage.toString());
  }

  private void verifyProperties(Errors errors) {
    // the code is required
    rejectIfEmptyOrWhitespace(errors, CODE, ProgramMessageKeys.ERROR_CODE_REQUIRED);
  }

  private void verifyCode(UUID id, Code code, Errors errors) {
    // program code cannot be duplicated
    Program db = programRepository.findByCode(code);

    if (null != db && (null == id || !id.equals(db.getId()))) {
      rejectValue(errors, CODE, ProgramMessageKeys.ERROR_CODE_DUPLICATED);
    }
  }
}
