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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.UserApiResponseDto;
import org.openlmis.referencedata.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserImportHelper {

  @Value("${referencedata.user.import.default.password}")
  private String defaultPassword;

  /**
   * Gets successfully created users based on API response.
   *
   * @param batch list of all users from batch
   * @param response API response
   * @return filtered list of {@link UserDto} objects - only successfully created users
   */
  public List<UserDto> getSuccessfullyCreatedUsers(List<UserDto> batch,
                                                   UserApiResponseDto response) {
    Set<UUID> successfulIds = response.getSuccessfulResults()
        .stream()
        .map(UserApiResponseDto.UserResponse::getReferenceDataUserId)
        .collect(Collectors.toSet());

    return batch.stream()
        .filter(userDto -> successfulIds.contains(userDto.getId()))
        .collect(Collectors.toList());
  }

  /**
   * Creates general ErrorDetails object with list of errors.
   *
   * @param message message with error
   * @param ex thrown exception
   * @return ErrorDetails object with error messages
   */
  public static ImportResponseDto.ErrorDetails createError(String message, Exception ex) {
    return new ImportResponseDto.ErrorDetails(
        Collections.singletonList(message + " " + ex.getMessage()));
  }

  /**
   * Adds errors to error list captured when importing users.
   *
   * @param response API response
   * @param batch list of users
   */
  public List<ImportResponseDto.ErrorDetails> collectErrorsFromResponse(
      UserApiResponseDto response, List<UserDto> batch) {
    List<ImportResponseDto.ErrorDetails> errors = new ArrayList<>();
    for (UserApiResponseDto.FailedUserResponse failedEntry : response.getFailedResults()) {
      batch.stream()
          .filter(user -> user.getId().equals(failedEntry.getReferenceDataUserId()))
          .findFirst()
          .ifPresent(dto -> errors.add(
              new ImportResponseDto.UserErrorDetails(failedEntry.getErrors(), dto.getUsername())));
    }
    return errors;
  }

  public String getDefaultUserPassword() {
    return defaultPassword;
  }
}
