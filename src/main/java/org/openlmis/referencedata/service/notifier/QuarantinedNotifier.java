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
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.SystemNotificationRepository;
import org.openlmis.referencedata.repository.UserSearchParams;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.service.notification.NotificationService;
import org.openlmis.referencedata.util.QuarantinedNotificationBuilderFactory;
import org.openlmis.referencedata.util.QuarantinedObjectEmailBuilder;
import org.openlmis.referencedata.util.QuarantinedObjectSystemNotificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class QuarantinedNotifier {
  /** How many users to process at a time. */
  static final int USER_BATCH_SIZE = 100;

  private final QuarantinedNotificationBuilderFactory notificationBuilderFactory;
  private final OrderableRepository orderableRepository;
  private final UserService userService;
  private final NotificationService notificationService;
  private final SystemNotificationRepository systemNotificationRepository;

  @Autowired
  QuarantinedNotifier(
      QuarantinedNotificationBuilderFactory notificationBuilderFactory,
      OrderableRepository orderableRepository,
      UserService userService,
      NotificationService notificationService,
      SystemNotificationRepository systemNotificationRepository) {
    this.notificationBuilderFactory = notificationBuilderFactory;
    this.orderableRepository = orderableRepository;
    this.userService = userService;
    this.notificationService = notificationService;
    this.systemNotificationRepository = systemNotificationRepository;
  }

  /** Notify ALL active users about a Lot being quarantined. */
  public void notifyLotQuarantine(Lot lot) {
    final Orderable orderable = orderableRepository.findLatestVersionByLotId(lot.getId());

    final QuarantinedObjectSystemNotificationBuilder systemNotificationBuilder =
        lot.isQuarantined()
            ? notificationBuilderFactory.forQuarantinedLotSystem(orderable, lot)
            : notificationBuilderFactory.forFreeLotSystem(orderable, lot);
    systemNotificationRepository.save(systemNotificationBuilder.buildSystemNotification());

    final QuarantinedObjectEmailBuilder emailBuilder =
        lot.isQuarantined()
            ? notificationBuilderFactory.forQuarantinedLotEmail(orderable, lot)
            : notificationBuilderFactory.forFreeLotEmail(orderable, lot);
    notifyViaEmail(emailBuilder);
  }

  /** Notify ALL active users about a Lot being quarantined. */
  public void notifyOrderableQuarantine(Orderable orderable) {
    final QuarantinedObjectSystemNotificationBuilder systemNotificationBuilder =
        orderable.isQuarantined()
            ? notificationBuilderFactory.forQuarantinedOrderableSystem(orderable)
            : notificationBuilderFactory.forFreeOrderableSystem(orderable);
    systemNotificationRepository.save(systemNotificationBuilder.buildSystemNotification());

    final QuarantinedObjectEmailBuilder emailBuilder =
        orderable.isQuarantined()
            ? notificationBuilderFactory.forQuarantinedOrderableEmail(orderable)
            : notificationBuilderFactory.forFreeOrderableEmail(orderable);
    notifyViaEmail(emailBuilder);
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
