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

import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_EMAIL_DUPLICATED;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_EMAIL_INVALID_FORMAT;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_EMAIL_TOO_LONG;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_FIRST_NAME_REQUIRED_FOR_ALL_USERS;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_JOB_TITLE_TOO_LONG;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_LAST_NAME_REQUIRED_FOR_ALL_USERS;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_PHONE_NUMBER_TOO_LONG;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_TIMEZONE_TOO_LONG;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_USERNAME_DUPLICATED;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_USERNAME_REQUIRED_FOR_ALL_USERS;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ERROR_USERNAME_TOO_LONG;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;

public class UserImportValidator {

  private static final int MAX_FIELD_LENGTH = 255;

  private static final String EMAIL_REGEX =
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]+$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  /**
   * Validates list of {@link UserDto} entries read from file.
   *
   * @param entries list of UserDto objects
   */
  public static void validateFileEntries(List<UserDto> entries) {
    validateMandatoryFields(entries);
    validateDuplicatedFields(entries);
    validateEmailFormats(entries);
    validateTooLongFields(entries);
  }

  private static void validateMandatoryFields(List<UserDto> entries) {
    boolean isAnyUsernameEmpty = entries.stream()
        .anyMatch(entry -> StringUtils.isBlank(entry.getUsername()));
    if (isAnyUsernameEmpty) {
      throw new ValidationMessageException(ERROR_USERNAME_REQUIRED_FOR_ALL_USERS);
    }

    boolean isAnyFirstNameEmpty = entries.stream()
        .anyMatch(entry -> StringUtils.isBlank(entry.getFirstName()));
    if (isAnyFirstNameEmpty) {
      throw new ValidationMessageException(ERROR_FIRST_NAME_REQUIRED_FOR_ALL_USERS);
    }

    boolean isAnyLastNameEmpty = entries.stream()
        .anyMatch(entry -> StringUtils.isBlank(entry.getLastName()));
    if (isAnyLastNameEmpty) {
      throw new ValidationMessageException(ERROR_LAST_NAME_REQUIRED_FOR_ALL_USERS);
    }
  }

  private static void validateDuplicatedFields(List<UserDto> entries) {
    List<String> duplicatedUsernames = entries.stream()
        .map(UserDto::getUsername)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    if (!duplicatedUsernames.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_USERNAME_DUPLICATED, String.join(", ", duplicatedUsernames)));
    }

    List<String> duplicatedEmails = entries.stream()
        .map(UserDto::getEmail)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    if (!duplicatedEmails.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_EMAIL_DUPLICATED, String.join(", ", duplicatedEmails)));
    }
  }

  private static void validateEmailFormats(List<UserDto> entries) {
    List<String> invalidEmails = entries.stream()
        .map(UserDto::getEmail)
        .filter(email -> email != null && !EMAIL_PATTERN.matcher(email).matches())
        .collect(Collectors.toList());

    if (!invalidEmails.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_EMAIL_INVALID_FORMAT, String.join(", ", invalidEmails)));
    }
  }

  private static void validateTooLongFields(List<UserDto> entries) {
    List<String> tooLongUsernames = entries.stream()
        .map(UserDto::getUsername)
        .filter(username -> username.length() > MAX_FIELD_LENGTH)
        .collect(Collectors.toList());

    if (!tooLongUsernames.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_USERNAME_TOO_LONG, String.join(", ", tooLongUsernames)));
    }

    List<String> tooLongEmails = entries.stream()
        .map(UserDto::getEmail)
        .filter(email -> email != null && email.length() > MAX_FIELD_LENGTH)
        .collect(Collectors.toList());

    if (!tooLongEmails.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_EMAIL_TOO_LONG, String.join(", ", tooLongEmails)));
    }

    List<String> tooLongPhoneNumbers = entries.stream()
        .map(UserDto::getPhoneNumber)
        .filter(phoneNumber -> phoneNumber != null && phoneNumber.length() > MAX_FIELD_LENGTH)
        .collect(Collectors.toList());

    if (!tooLongPhoneNumbers.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHONE_NUMBER_TOO_LONG, String.join(", ", tooLongPhoneNumbers)));
    }

    List<String> tooLongTimezones = entries.stream()
        .map(UserDto::getTimezone)
        .filter(timezone -> timezone != null && timezone.length() > MAX_FIELD_LENGTH)
        .collect(Collectors.toList());

    if (!tooLongTimezones.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_TIMEZONE_TOO_LONG, String.join(", ", tooLongTimezones)));
    }

    List<String> tooLongJobTitles = entries.stream()
        .map(UserDto::getJobTitle)
        .filter(jobTitle -> jobTitle != null && jobTitle.length() > MAX_FIELD_LENGTH)
        .collect(Collectors.toList());

    if (!tooLongJobTitles.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_JOB_TITLE_TOO_LONG, String.join(", ", tooLongJobTitles)));
    }
  }
}
