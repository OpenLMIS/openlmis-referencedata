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

package org.openlmis.referencedata.service.notifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.repository.SystemNotificationRepository;
import org.openlmis.referencedata.repository.UserSearchParams;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.service.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class QuarantinedNotifierTest {
  private static final int TEST_USER_COUNT =
      (int) Math.ceil(QuarantinedNotifier.USER_BATCH_SIZE * 2.5);

  @Mock private AuthenticationHelper authenticationHelper;
  @Mock private MessageService messageService;
  @Mock private UserService userService;
  @Mock private NotificationService notificationService;
  @Mock private SystemNotificationRepository systemNotificationRepository;

  @InjectMocks private QuarantinedNotifier quarantinedNotifier;

  private List<User> testUsers = new ArrayList<>();

  @Before
  public void setupTests() {
    for (int i = 0; i < TEST_USER_COUNT; ++i) {
      final User user = mock(User.class);
      when(user.getId()).thenReturn(UUID.randomUUID());
      testUsers.add(user);
    }

    when(authenticationHelper.getCurrentUser()).thenReturn(mock(User.class));
    when(messageService.localizeString(anyString(), any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(userService.searchUsers(any(UserSearchParams.class), any(Pageable.class)))
        .thenAnswer(invocation -> createTestUsersPage(invocation.getArgument(1)));
  }

  private Page<User> createTestUsersPage(Pageable pageRequest) {
    if (pageRequest.isPaged()) {
      return new PageImpl<>(
          testUsers.subList(
              (int) pageRequest.getOffset(),
              Math.min(
                  testUsers.size(), (int) pageRequest.getOffset() + pageRequest.getPageSize())),
          pageRequest,
          testUsers.size());
    } else {
      return new PageImpl<>(testUsers);
    }
  }

  @Test
  public void notifyLotQuarantine_shouldSendEmailAndSystemNotification() {
    final Lot testLot = mock(Lot.class);

    quarantinedNotifier.notifyLotQuarantine(testLot);

    for (User testUser : testUsers) {
      verify(notificationService, times(1))
          .notifyAsyncEmail(eq(testUser.getId()), anyString(), anyString());
    }
    verify(systemNotificationRepository, times(1)).save(any());
  }
}
