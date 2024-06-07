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

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.repository.SystemNotificationRepository;
import org.openlmis.referencedata.repository.UserSearchParams;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.service.notification.NotificationService;
import org.openlmis.referencedata.util.QuarantinedLotEmailBuilder;
import org.openlmis.referencedata.util.QuarantinedLotSystemNotificationBuilder;
import org.openlmis.referencedata.util.QuarantinedObjectEmailBuilder;
import org.openlmis.referencedata.util.QuarantinedOrderableEmailBuilder;
import org.openlmis.referencedata.util.QuarantinedOrderableSystemNotificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class QuarantinedNotifier {
  /** How many users to process at a time. */
  static final int USER_BATCH_SIZE = 100;

  @Autowired private AuthenticationHelper authenticationHelper;
  @Autowired private MessageService messageService;
  @Autowired private UserService userService;
  @Autowired private NotificationService notificationService;
  @Autowired private SystemNotificationRepository systemNotificationRepository;

  /** Notify ALL active users about a Lot being quarantined. */
  public void notifyLotQuarantine(Lot lot) {
    systemNotificationRepository.save(
        new QuarantinedLotSystemNotificationBuilder(
                messageService, authenticationHelper.getCurrentUser(), lot)
            .buildSystemNotification());

    notifyViaEmail(new QuarantinedLotEmailBuilder(messageService, lot));
  }

  /** Notify ALL active users about a Lot being quarantined. */
  public void notifyOrderableQuarantine(Orderable orderable) {
    systemNotificationRepository.save(
        new QuarantinedOrderableSystemNotificationBuilder(
                messageService, authenticationHelper.getCurrentUser(), orderable)
            .buildSystemNotification());

    notifyViaEmail(new QuarantinedOrderableEmailBuilder(messageService, orderable));
  }

  private void notifyViaEmail(QuarantinedObjectEmailBuilder emailBuilder) {
    Pageable pageRequest = PageRequest.of(0, USER_BATCH_SIZE);
    do {
      final Page<User> usersPage =
          userService.searchUsers(UserSearchParams.builder().active(true).build(), pageRequest);

      for (User recipient : usersPage.getContent()) {
        final QuarantinedObjectEmailBuilder.Email email = emailBuilder.buildEmail(recipient);
        notificationService.notifyAsyncEmail(
            recipient.getId(), email.getTitle(), email.getContent());
      }

      pageRequest = usersPage.nextPageable();
    } while (pageRequest.isPaged());
  }
}
