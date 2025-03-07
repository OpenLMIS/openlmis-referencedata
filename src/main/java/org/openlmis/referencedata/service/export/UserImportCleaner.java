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
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserAuthService;
import org.openlmis.referencedata.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserImportCleaner {

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserRepository userRepository;

  /**
   * Removes inconsistent data created during user import.
   *
   * @param persistedUsers list of successfully created users during import
   * @param successfulAuthDetails list of successfully created auth user details during import
   */
  public void cleanupInconsistentData(List<UserDto> persistedUsers,
                                       List<UserDto> successfulAuthDetails) {
    Set<UUID> persistedUserIds = extractUserIds(persistedUsers);
    Set<UUID> authIds = extractUserIds(successfulAuthDetails);

    if (successfulAuthDetails.size() < persistedUsers.size()) {
      removeUsersData(persistedUserIds, authIds);
    }
  }

  private Set<UUID> extractUserIds(List<UserDto> users) {
    return users.stream().map(UserDto::getId).collect(Collectors.toSet());
  }

  private void removeUsersData(Set<UUID> persistedUserIds, Set<UUID> validIds) {
    Set<UUID> idsToRemove = new HashSet<>(persistedUserIds);
    idsToRemove.removeAll(validIds);

    if (!idsToRemove.isEmpty()) {
      userAuthService.deleteAuthUsersByUserUuids(idsToRemove);
      userDetailsService.deleteUserContactDetailsByUserUuids(idsToRemove);
      userRepository.deleteUsersByIds(idsToRemove);
    }
  }
}
