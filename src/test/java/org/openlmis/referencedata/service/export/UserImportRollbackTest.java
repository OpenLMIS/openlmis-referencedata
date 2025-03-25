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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.UserAuthService;
import org.openlmis.referencedata.service.UserDetailsService;
import org.openlmis.referencedata.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserImportRollbackTest {
  @Mock
  private UserAuthService userAuthService;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private UserService userService;

  @InjectMocks
  private UserImportRollback userImportRollback;

  private UUID userId1;
  private UUID userId2;
  private UserDto user1;
  private UserDto user2;

  @Before
  public void setUp() {
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();

    user1 = new UserDto();
    user1.setId(userId1);
    user1.setUsername("John");

    user2 = new UserDto();
    user2.setId(userId2);
    user2.setUsername("Paul");
  }

  @Test
  public void shouldRemoveUsersWhenAuthDetailsAreMissing() {
    List<UserDto> persistedUsers = Arrays.asList(user1, user2);
    List<UserDto> successfulAuthDetails = Collections.singletonList(user1);
    Map<String, Boolean> userStatusMap = new HashMap<>();
    userStatusMap.put("Paul", true);

    userImportRollback.cleanupInconsistentData(
        persistedUsers, successfulAuthDetails, userStatusMap);

    Set<UUID> expectedIdsToRemove = Collections.singleton(userId2);

    verify(userAuthService).deleteAuthUsersByUserUuids(expectedIdsToRemove);
    verify(userDetailsService).deleteUserContactDetailsByUserUuids(expectedIdsToRemove);
    verify(userService).deleteUsersByIds(expectedIdsToRemove);
  }

  @Test
  public void shouldNotRemoveUsersWhenAllHaveAuthDetails() {
    List<UserDto> persistedUsers = Arrays.asList(user1, user2);
    List<UserDto> successfulAuthDetails = Arrays.asList(user1, user2);
    Map<String, Boolean> userStatusMap = Collections.emptyMap();

    userImportRollback.cleanupInconsistentData(
        persistedUsers, successfulAuthDetails, userStatusMap);

    verify(userAuthService, never()).deleteAuthUsersByUserUuids(any());
    verify(userDetailsService, never()).deleteUserContactDetailsByUserUuids(any());
    verify(userService, never()).deleteUsersByIds(any());
  }

  @Test
  public void shouldNotRemoveUsersWhenNoUsersPersisted() {
    List<UserDto> persistedUsers = Collections.emptyList();
    List<UserDto> successfulAuthDetails = Collections.emptyList();
    Map<String, Boolean> userStatusMap = Collections.emptyMap();

    userImportRollback.cleanupInconsistentData(
        persistedUsers, successfulAuthDetails, userStatusMap);

    verify(userAuthService, never()).deleteAuthUsersByUserUuids(any());
    verify(userDetailsService, never()).deleteUserContactDetailsByUserUuids(any());
    verify(userService, never()).deleteUsersByIds(any());
  }
}
