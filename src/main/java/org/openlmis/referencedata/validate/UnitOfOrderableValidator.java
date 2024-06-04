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

import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.DISPLAY_ORDER;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.ERROR_DISPLAY_ORDER_MUST_BE_POSITIVE_OR_ZERO;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.ERROR_DISPLAY_ORDER_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.ERROR_FACTOR_MUST_BE_POSITIVE_OR_ZERO;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.ERROR_FACTOR_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.ERROR_NAME_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.ERROR_NULL;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.FACTOR;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.NAME;

import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UnitOfOrderableValidator implements BaseValidator {

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link UnitOfOrderableDto} class definition.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return UnitOfOrderableDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of
   * {@link UnitOfOrderableDto} class.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, ERROR_NULL);

    rejectIfEmptyOrWhitespace(errors, NAME, ERROR_NAME_REQUIRED);
    rejectIfNull(errors, DISPLAY_ORDER, ERROR_DISPLAY_ORDER_REQUIRED);
    rejectIfNull(errors, FACTOR, ERROR_FACTOR_REQUIRED);

    UnitOfOrderableDto unitOfOrderableDto = (UnitOfOrderableDto) target;
    validateNumber(unitOfOrderableDto.getDisplayOrder(), errors,
        DISPLAY_ORDER, ERROR_DISPLAY_ORDER_MUST_BE_POSITIVE_OR_ZERO);
    validateNumber(unitOfOrderableDto.getFactor(), errors,
        FACTOR, ERROR_FACTOR_MUST_BE_POSITIVE_OR_ZERO);
  }

  private void validateNumber(Integer number, Errors errors, String fieldName,
                              String errorMessage) {
    if (number < 0) {
      rejectValue(errors, fieldName, errorMessage);
    }
  }
}
