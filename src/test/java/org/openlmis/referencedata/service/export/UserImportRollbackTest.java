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
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ImportedUserItemDto;
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
  private User user1;
  ImportedUserItemDto user1Item;
  private User user2;
  ImportedUserItemDto user2Item;

  @Before
  public void setUp() {
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();

    user1 = new User();
    user1.setId(userId1);
    user1.setUsername("John");
    user1Item = new ImportedUserItemDto(user1, true);

    user2 = new User();
    user2.setId(userId2);
    user2.setUsername("Paul");
    user2Item = new ImportedUserItemDto(user2, true);
  }

  @Test
  public void shouldRemoveUsersWhenAuthDetailsAreMissing() {
    UserImportResult userImportResult = new UserImportResult();
    userImportResult.setSuccessfulUsers(Arrays.asList(user1Item, user2Item));
    userImportResult.setSuccessfulAuthDetails(
        Collections.singletonList(UserDto.newInstance(user1)));

    userImportRollback.cleanupInconsistentData(userImportResult);

    Set<UUID> expectedIdsToRemove = Collections.singleton(userId2);

    verify(userAuthService).deleteAuthUsersByUserUuids(expectedIdsToRemove);
    verify(userDetailsService).deleteUserContactDetailsByUserUuids(expectedIdsToRemove);
    verify(userService).deleteUsersByIds(expectedIdsToRemove);
  }

  @Test
  public void shouldNotRemoveUsersWhenAllHaveAuthDetails() {
    UserImportResult userImportResult = new UserImportResult();
    userImportResult.setSuccessfulUsers(Arrays.asList(user1Item, user2Item));
    userImportResult.setSuccessfulAuthDetails(
        Arrays.asList(UserDto.newInstance(user1), UserDto.newInstance(user2)));

    userImportRollback.cleanupInconsistentData(userImportResult);

    verify(userAuthService, never()).deleteAuthUsersByUserUuids(any());
    verify(userDetailsService, never()).deleteUserContactDetailsByUserUuids(any());
    verify(userService, never()).deleteUsersByIds(any());
  }

  @Test
  public void shouldNotRemoveUsersWhenNoUsersPersisted() {
    userImportRollback.cleanupInconsistentData(new UserImportResult(Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList()));

    verify(userAuthService, never()).deleteAuthUsersByUserUuids(any());
    verify(userDetailsService, never()).deleteUserContactDetailsByUserUuids(any());
    verify(userService, never()).deleteUsersByIds(any());
  }
}
