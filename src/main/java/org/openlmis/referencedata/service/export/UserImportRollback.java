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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ImportedUserItemDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.UserAuthService;
import org.openlmis.referencedata.service.UserDetailsService;
import org.openlmis.referencedata.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserImportRollback {

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserService userService;

  /**
   * Removes inconsistent data created during user import.
   *
   * @param userImportResult object with import result lists
   */
  public void cleanupInconsistentData(UserImportResult userImportResult) {
    if (userImportResult.getSuccessfulAuthDetails().size()
        < userImportResult.getSuccessfulUsers().size()) {
      removeUsersData(userImportResult);
    }
  }

  private Set<UUID> extractUserIdsFromUserDtos(List<UserDto> users) {
    return users.stream().map(UserDto::getId).collect(Collectors.toSet());
  }

  private Set<UUID> extractUserIdsFromUsers(List<User> users) {
    return users.stream().map(User::getId).collect(Collectors.toSet());
  }

  private void removeUsersData(UserImportResult userImportResult) {
    List<ImportedUserItemDto> persistedUsers = userImportResult.getSuccessfulUsers();
    Set<UUID> idsToRemove = new HashSet<>(extractUserIdsFromUsers(persistedUsers.stream()
        .map(ImportedUserItemDto::getUser)
        .collect(Collectors.toList())));
    idsToRemove.removeAll(extractUserIdsFromUserDtos(userImportResult.getSuccessfulAuthDetails()));

    Set<UUID> filteredIdsToRemove = persistedUsers.stream()
        .filter(item -> idsToRemove.contains(item.getUser().getId()))
        .filter(item -> Boolean.TRUE.equals(item.isNewUser()))
        .map(item -> item.getUser().getId())
        .collect(Collectors.toSet());

    if (!filteredIdsToRemove.isEmpty()) {
      userAuthService.deleteAuthUsersByUserUuids(filteredIdsToRemove);
      userDetailsService.deleteUserContactDetailsByUserUuids(filteredIdsToRemove);
      userService.deleteUsersByIds(filteredIdsToRemove);
    }
  }
}
