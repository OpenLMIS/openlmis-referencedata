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

import java.time.ZonedDateTime;
import java.util.UUID;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.dto.SystemNotificationDto;
import org.openlmis.referencedata.repository.SystemNotificationRepository;
import org.openlmis.referencedata.util.messagekeys.SystemNotificationMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ValidationMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SystemNotificationValidator implements BaseValidator {

  private static final String CREATED_DATE = "createdDate";
  private static final String START_DATE = "startDate";
  private static final String EXPIRY_DATE = "expiryDate";
  private static final String MESSAGE = "message";
  private static final String AUTHOR = "author";
  private static final String ACTIVE = "active";

  @Autowired
  private SystemNotificationRepository repository;

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link SystemNotificationDto} class definition.
   *     Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return SystemNotificationDto.class.equals(clazz);
  }

  /**
   * Validates the object, which must be an instance of {@link SystemNotificationDto} class.
   *
   * <p>Firstly, the method checks if the target object has a value in {@code message},
   * {@code author}, {@code active} and {@code createdDate} properties.
   * <p>Secondly, the method checks if the {@code author} and {@code createdDate} params
   * are not changed.</p>
   * <p>If there are no errors, the method checks if the {@code startDate} property in the target
   * object is before the {@code expiryDate}.</p>
   *
   * @param obj the object that is to be validated
   * @param err contextual state about the validation process
   */
  @Override
  public void validate(Object obj, Errors err) {
    rejectIfEmpty(err, MESSAGE, SystemNotificationMessageKeys.ERROR_MESSAGE_REQUIRED);
    rejectIfEmpty(err, AUTHOR, SystemNotificationMessageKeys.ERROR_AUTHOR_REQUIRED);
    rejectIfEmpty(err, ACTIVE, SystemNotificationMessageKeys.ERROR_ACTIVE_FLAG_REQUIRED);
    rejectIfEmpty(err, CREATED_DATE, SystemNotificationMessageKeys.ERROR_CREATED_DATE_REQUIRED);
    if (!err.hasErrors()) {
      SystemNotification notification = (SystemNotification) obj;
      UUID notificationId = notification.getId();
      SystemNotification existingNotification = (notificationId != null)
          ? repository.findOne(notificationId) : null;
      if (existingNotification != null) {
        rejectIfValueChanged(err, notification.getAuthor(),
            existingNotification.getAuthor(), AUTHOR);
        rejectIfValueChanged(err, notification.getCreatedDate(),
            existingNotification.getCreatedDate(), CREATED_DATE);
      }

      ZonedDateTime startDate = notification.getStartDate();
      ZonedDateTime expiryDate = notification.getExpiryDate();

      if (expiryDate.isBefore(startDate)) {
        rejectValue(err, START_DATE,
            SystemNotificationMessageKeys.ERROR_START_DATE_AFTER_EXPIRY_DATE);
        rejectValue(err, EXPIRY_DATE,
            SystemNotificationMessageKeys.ERROR_EXPIRY_DATE_BEFORE_START_DATE);
      }
    }
  }

  private void rejectIfValueChanged(Errors errors, Object value, Object savedValue, String field) {
    if (value != null && savedValue != null && !savedValue.equals(value)) {
      rejectValue(errors, field, ValidationMessageKeys.ERROR_IS_INVARIANT, field);
    }
  }

}
