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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
public class UserImportValidatorTest {
  private UserDto user1;
  private UserDto user2;
  private UserDto user3;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    user1 = generateUserDto();
    user2 = generateUserDto();
    user3 = generateUserDto();
  }

  @Test
  public void shouldThrowExceptionWhenAnyUsernameIsEmpty() {
    user2.setUsername(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.username.required.forAllUsers");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenAnyFirstNameIsEmpty() {
    user2.setFirstName(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.firstName.required.forAllUsers");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenAnyLastNameIsEmpty() {
    user2.setLastName(null);

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.lastName.required.forAllUsers");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenAnyUsernameIsDuplicated() {
    user2.setUsername("john1");
    user3.setUsername("john1");

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.username.duplicated");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenAnyEmailIsDuplicated() {
    user2.setEmail("john1@pl.pl");
    user3.setEmail("john1@pl.pl");

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.email.duplicated");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenWrongEmailFormat() {
    user2.setEmail("john1pl.pl");

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.email.invalid.format");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenUsernameTooLong() {
    user2.setUsername(String.join("", Collections.nCopies(300, "a")));

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.username.too.long");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenEmailTooLong() {
    user2.setEmail(String.join("", Collections.nCopies(300, "a")) + "@pl.pl");

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.email.too.long");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenPhoneNumberTooLong() {
    user2.setPhoneNumber(String.join("", Collections.nCopies(300, "2")));

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.phoneNumber.too.long");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenTimezoneTooLong() {
    user2.setTimezone(String.join("", Collections.nCopies(100, "UTC")));

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.timezone.too.long");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  @Test
  public void shouldThrowExceptionWhenJobTitleTooLong() {
    user2.setJobTitle(String.join("", Collections.nCopies(100, "dev")));

    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(
        "referenceData.error.user.import.jobTitle.too.long");

    UserImportValidator.validateFileEntries(createEntryList());
  }

  private List<UserDto> createEntryList() {
    return Arrays.asList(user1, user2, user3);
  }

  private UserDto generateUserDto() {
    return UserDto.newInstance(new UserDataBuilder().buildAsNew());
  }
}
