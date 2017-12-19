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

import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.UUID;

/**
 * A validator for {@link UserDto} object.
 */
@Component
public class UserValidator implements BaseValidator {

  // User fields
  static final String USERNAME = "username";
  static final String EMAIL = "email";
  static final String FIRST_NAME = "firstName";
  static final String LAST_NAME = "lastName";

  @Autowired
  private UserRepository userRepository;

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link UserDto} class definition.
   *     Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return UserDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of {@link UserDto} class.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, UserMessageKeys.ERROR_NULL);

    rejectEmptyValues(errors);
    if (!errors.hasErrors()) {
      UserDto user = (UserDto) target;
      verifyUsername(user.getId(), user.getUsername(), errors);

      if (user.getEmail() != null) {
        verifyEmail(user.getId(), user.getEmail(), errors);
      }
    }
  }

  private void rejectEmptyValues(Errors errors) {
    rejectIfEmptyOrWhitespace(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, FIRST_NAME, UserMessageKeys.ERROR_FIRSTNAME_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, LAST_NAME, UserMessageKeys.ERROR_LASTNAME_REQUIRED);

    if (errors.getFieldValue(EMAIL) != null) {
      rejectIfEmptyOrWhitespace(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_INVALID);
    }
  }

  private void verifyUsername(UUID id, String username, Errors errors) {
    // user name cannot contains invalid characters
    if (!username.matches("\\w+")) {
      rejectValue(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_INVALID);
    }

    // user name cannot be duplicated
    User db = userRepository.findOneByUsernameIgnoreCase(username);

    if (null != db && (null == id || !id.equals(db.getId()))) {
      rejectValue(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_DUPLICATED);
    }
  }

  private void verifyEmail(UUID id, String email, Errors errors) {
    // user email cannot be duplicated
    User db = userRepository.findOneByEmail(email);

    if (null != db && (null == id || !id.equals(db.getId()))) {
      rejectValue(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_DUPLICATED);
    }

    if (!EmailValidator.getInstance().isValid(email)) {
      rejectValue(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_INVALID);
    }

  }
}
