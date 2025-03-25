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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
   * @param persistedUsers list of successfully created users during import
   * @param successfulAuthDetails list of successfully created auth user details during import
   */
  public void cleanupInconsistentData(List<UserDto> persistedUsers,
                                       List<UserDto> successfulAuthDetails,
                                      Map<String, Boolean> newUserStatusMap) {
    if (successfulAuthDetails.size() < persistedUsers.size()) {
      removeUsersData(persistedUsers, successfulAuthDetails, newUserStatusMap);
    }
  }

  private Set<UUID> extractUserIds(List<UserDto> users) {
    return users.stream().map(UserDto::getId).collect(Collectors.toSet());
  }

  private void removeUsersData(List<UserDto> persistedUsers, List<UserDto> successfulAuthDetails,
                               Map<String, Boolean> newUserStatusMap) {
    Set<UUID> idsToRemove = new HashSet<>(extractUserIds(persistedUsers));
    idsToRemove.removeAll(extractUserIds(successfulAuthDetails));

    Set<UUID> filteredIdsToRemove = persistedUsers.stream()
        .filter(user -> idsToRemove.contains(user.getId()))
        .filter(user -> Boolean.TRUE.equals(newUserStatusMap.get(user.getUsername())))
        .map(UserDto::getId)
        .collect(Collectors.toSet());

    if (!filteredIdsToRemove.isEmpty()) {
      userAuthService.deleteAuthUsersByUserUuids(filteredIdsToRemove);
      userDetailsService.deleteUserContactDetailsByUserUuids(filteredIdsToRemove);
      userService.deleteUsersByIds(filteredIdsToRemove);
    }
  }
}
