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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.openlmis.referencedata.validate.UserValidator.EMAIL;
import static org.openlmis.referencedata.validate.UserValidator.FIRSTNAME;
import static org.openlmis.referencedata.validate.UserValidator.LASTNAME;
import static org.openlmis.referencedata.validate.UserValidator.USERNAME;
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class UserValidatorTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private Validator validator = new UserValidator();

  private UserDto userDto;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    userDto = new UserDto();
    userDto.setUsername("User_name1");
    userDto.setEmail("user@mail.com");
    userDto.setFirstName("FirstName");
    userDto.setLastName("LastName");
    userDto.setId(UUID.randomUUID());

    errors = new BeanPropertyBindingResult(userDto, "userDto");
  }

  @Test
  public void shouldNotFindErrorsWhenUserIsValid() throws Exception {
    validator.validate(userDto, errors);

    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void shouldRejectIfUsernameIsDuplicatedAndIdNull() {
    userDto.setId(null);
    doReturn(mock(User.class))
        .when(userRepository)
        .findOneByUsername(userDto.getUsername());

    validator.validate(userDto, errors);

    assertErrorMessage(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_DUPLICATED);
  }

  @Test
  public void shouldNotRejectIfUsernameIsDuplicatedAndIdsAreSame() {
    User old = new User();
    old.setId(userDto.getId());

    doReturn(old)
        .when(userRepository)
        .findOneByUsername(userDto.getUsername());

    validator.validate(userDto, errors);
    assertThat(errors.hasFieldErrors(USERNAME), is(false));
  }

  @Test
  public void shouldRejectIfUserNameIsDuplicatedAndIdsAreDifferent() {
    User old = new User();
    old.setId(UUID.randomUUID());

    doReturn(old)
        .when(userRepository)
        .findOneByUsername(userDto.getUsername());

    validator.validate(userDto, errors);
    assertErrorMessage(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_DUPLICATED);
  }

  @Test
  public void shouldRejectWhenUsernameIsNull() {
    userDto.setUsername(null);

    validator.validate(userDto, errors);

    assertErrorMessage(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenUsernameIsEmpty() {
    userDto.setUsername("");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenUsernameContainsWhitespace() {
    userDto.setUsername("user name");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, USERNAME, UserMessageKeys.ERROR_USERNAME_INVALID);
  }

  @Test
  public void shouldRejectIfEmailIsDuplicated() {
    userDto.setId(null);
    doReturn(mock(User.class))
        .when(userRepository)
        .findOneByEmail(userDto.getEmail());

    validator.validate(userDto, errors);

    assertErrorMessage(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_DUPLICATED);
  }

  @Test
  public void shouldNotRejectIfEmailIsDuplicatedAndIdsAreSame() {
    User old = new User();
    old.setId(userDto.getId());

    doReturn(old)
        .when(userRepository)
        .findOneByEmail(userDto.getEmail());

    validator.validate(userDto, errors);
    assertThat(errors.hasFieldErrors(EMAIL), is(false));
  }

  @Test
  public void shouldRejectIfEmailIsDuplicatedAndIdsAreDifferent() {
    User old = new User();
    old.setId(UUID.randomUUID());

    doReturn(old)
        .when(userRepository)
        .findOneByEmail(userDto.getEmail());

    validator.validate(userDto, errors);
    assertErrorMessage(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_DUPLICATED);
  }

  @Test
  public void shouldRejectWhenEmailIsNull() {
    userDto.setEmail(null);

    validator.validate(userDto, errors);

    assertErrorMessage(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_REQUIRED);
  }

  @Test
  public void shouldRejectWhenEmailIsEmpty() {
    userDto.setEmail("");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_REQUIRED);
  }

  @Test
  public void shouldRejectWhenEmailIsWhitespace() {
    userDto.setEmail(" ");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, EMAIL, UserMessageKeys.ERROR_EMAIL_REQUIRED);
  }

  @Test
  public void shouldRejectWhenFirstNameIsNull() {
    userDto.setFirstName(null);

    validator.validate(userDto, errors);

    assertErrorMessage(errors, FIRSTNAME, UserMessageKeys.ERROR_FIRSTNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenFirstNameIsEmpty() {
    userDto.setFirstName("");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, FIRSTNAME, UserMessageKeys.ERROR_FIRSTNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenFirstNameIsWhitespace() {
    userDto.setFirstName(" ");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, FIRSTNAME, UserMessageKeys.ERROR_FIRSTNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLastNameIsNull() {
    userDto.setLastName(null);

    validator.validate(userDto, errors);

    assertErrorMessage(errors, LASTNAME, UserMessageKeys.ERROR_LASTNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLastNameIsEmpty() {
    userDto.setLastName("");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, LASTNAME, UserMessageKeys.ERROR_LASTNAME_REQUIRED);
  }

  @Test
  public void shouldRejectWhenLastNameIsWhitespace() {
    userDto.setLastName(" ");

    validator.validate(userDto, errors);

    assertErrorMessage(errors, LASTNAME, UserMessageKeys.ERROR_LASTNAME_REQUIRED);
  }
}
