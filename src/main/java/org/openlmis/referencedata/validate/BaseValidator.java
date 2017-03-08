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

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

interface BaseValidator extends Validator {

  default void rejectIfEmpty(Errors errors, String field, String message) {
    ValidationUtils.rejectIfEmpty(errors, field, message, message);
  }

  default void rejectIfEmptyOrWhitespace(Errors errors, String field, String message) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, message, message);
  }

  default void rejectValue(Errors errors, String field, String message) {
    errors.rejectValue(field, message, message);
  }

  default void rejectValue(Errors errors, String field, String message, String... parameters) {
    errors.rejectValue(field, message, parameters, message);
  }
}
