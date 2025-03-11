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

package org.openlmis.referencedata.service.export;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.UserApiResponseDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserImportHelperTest {

  @Autowired
  private UserImportHelper userImportHelper;

  @Test
  public void shouldReturnSuccessfullyCreatedUsers() {
    UUID successfulId = UUID.randomUUID();
    UUID failedId = UUID.randomUUID();

    UserDto user1 = new UserDto();
    user1.setId(successfulId);
    user1.setUsername("successUser");

    UserDto user2 = new UserDto();
    user2.setId(failedId);
    user2.setUsername("failedUser");

    List<UserDto> batch = Arrays.asList(user1, user2);

    UserApiResponseDto response = new UserApiResponseDto(
        Collections.singletonList(new UserApiResponseDto.UserResponse(successfulId)),
        Collections.emptyList()
    );

    List<UserDto> result = userImportHelper.getSuccessfullyCreatedUsers(batch, response);

    assertEquals(1, result.size());
    assertEquals("successUser", result.get(0).getUsername());
  }

  @Test
  public void shouldReturnEmptyListWhenNoSuccessUsers() {
    UserDto user = new UserDto();
    user.setId(UUID.randomUUID());
    user.setUsername("john");

    List<UserDto> batch = Collections.singletonList(user);

    UserApiResponseDto response = new UserApiResponseDto(
        Collections.emptyList(),
        Collections.emptyList()
    );

    List<UserDto> result = userImportHelper.getSuccessfullyCreatedUsers(batch, response);

    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldCreateErrorWithMessage() {
    String message = "Import failed";
    Exception ex = new ValidationMessageException("Server error");

    ImportResponseDto.ErrorDetails response = UserImportHelper.createError(message, ex);

    assertEquals(1, response.getErrors().size());
    assertThat(response.getErrors().get(0), containsString("Import failed Server error"));
  }

  @Test
  public void shouldAddErrorsFromResponse() {
    UUID failedId = UUID.randomUUID();
    UserDto failedUser = new UserDto();
    failedUser.setId(failedId);
    failedUser.setUsername("failedUser");

    List<UserDto> batch = Collections.singletonList(failedUser);
    List<String> errorMessages = Arrays.asList("invalid email", "username too long");

    UserApiResponseDto.FailedUserResponse failedResponse =
        new UserApiResponseDto.FailedUserResponse(errorMessages);
    failedResponse.setReferenceDataUserId(failedId);

    UserApiResponseDto response = new UserApiResponseDto(
        Collections.emptyList(),
        Collections.singletonList(failedResponse)
    );

    List<ImportResponseDto.ErrorDetails> errors = new ArrayList<>();

    userImportHelper.addErrorsFromResponse(response, errors, batch);

    assertEquals(1, errors.size());
    assertEquals(errorMessages, errors.get(0).getErrors());
  }
}
