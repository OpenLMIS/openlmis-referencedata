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

package org.openlmis.referencedata.util;

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.util.messagekeys.NotificationMessageKeys;
import org.openlmis.referencedata.web.locale.LocaleDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class QuarantinedNotificationBuilderFactory {
  private final MessageService messageService;
  private final LocaleDtoBuilder localeDtoBuilder;
  private final AuthenticationHelper authenticationHelper;

  @Autowired
  QuarantinedNotificationBuilderFactory(
      MessageService messageService,
      LocaleDtoBuilder localeDtoBuilder,
      AuthenticationHelper authenticationHelper) {
    this.messageService = messageService;
    this.localeDtoBuilder = localeDtoBuilder;
    this.authenticationHelper = authenticationHelper;
  }

  /**
   * Creates System Notification Builder for Quarantined Lot.
   *
   * @param orderable the lot's orderable, not null
   * @param lot the lot, not null
   * @return the builder, not null
   */
  public QuarantinedObjectSystemNotificationBuilder forQuarantinedLotSystem(
      Orderable orderable, Lot lot) {
    return new QuarantinedObjectSystemNotificationBuilder(
        messageService,
        localeDtoBuilder.build(),
        authenticationHelper.getCurrentUser(),
        NotificationMessageKeys.LOT_QUARANTINED_SYSTEM_NOTIFICATION_TITLE_TEMPLATE,
        NotificationMessageKeys.LOT_QUARANTINED_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE,
        () -> orderable.getFullProductName() + " - " + lot.getLotCode());
  }

  /**
   * Creates System Notification Builder for Free Lot.
   *
   * @param orderable the lot's orderable, not null
   * @param lot the lot, not null
   * @return the builder, not null
   */
  public QuarantinedObjectSystemNotificationBuilder forFreeLotSystem(Orderable orderable, Lot lot) {
    return new QuarantinedObjectSystemNotificationBuilder(
        messageService,
        localeDtoBuilder.build(),
        authenticationHelper.getCurrentUser(),
        NotificationMessageKeys.LOT_FREE_SYSTEM_NOTIFICATION_TITLE_TEMPLATE,
        NotificationMessageKeys.LOT_FREE_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE,
        () -> orderable.getFullProductName() + " - " + lot.getLotCode());
  }

  /**
   * Creates Email Notification Builder for Quarantined Lot.
   *
   * @param orderable the lot's orderable, not null
   * @param lot the lot, not null
   * @return the builder, not null
   */
  public QuarantinedObjectEmailBuilder forQuarantinedLotEmail(Orderable orderable, Lot lot) {
    return new QuarantinedObjectEmailBuilder(
        messageService,
        localeDtoBuilder.build(),
        NotificationMessageKeys.LOT_QUARANTINED_EMAIL_TITLE_TEMPLATE,
        NotificationMessageKeys.LOT_QUARANTINED_EMAIL_CONTENT_TEMPLATE,
        () -> orderable.getFullProductName() + " - " + lot.getLotCode());
  }

  /**
   * Creates Email Notification Builder for Free Lot.
   *
   * @param orderable the lot's orderable, not null
   * @param lot the lot, not null
   * @return the builder, not null
   */
  public QuarantinedObjectEmailBuilder forFreeLotEmail(Orderable orderable, Lot lot) {
    return new QuarantinedObjectEmailBuilder(
        messageService,
        localeDtoBuilder.build(),
        NotificationMessageKeys.LOT_FREE_EMAIL_TITLE_TEMPLATE,
        NotificationMessageKeys.LOT_FREE_EMAIL_CONTENT_TEMPLATE,
        () -> orderable.getFullProductName() + " - " + lot.getLotCode());
  }

  /**
   * Creates System Notification Builder for Quarantined Orderable.
   *
   * @param orderable the lot's orderable, not null
   * @return the builder, not null
   */
  public QuarantinedObjectSystemNotificationBuilder forQuarantinedOrderableSystem(
      Orderable orderable) {
    return new QuarantinedObjectSystemNotificationBuilder(
        messageService,
        localeDtoBuilder.build(),
        authenticationHelper.getCurrentUser(),
        NotificationMessageKeys.ORDERABLE_QUARANTINED_SYSTEM_NOTIFICATION_TITLE_TEMPLATE,
        NotificationMessageKeys.ORDERABLE_QUARANTINED_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE,
        orderable::getFullProductName);
  }

  /**
   * Creates System Notification Builder for Free Orderable.
   *
   * @param orderable the lot's orderable, not null
   * @return the builder, not null
   */
  public QuarantinedObjectSystemNotificationBuilder forFreeOrderableSystem(Orderable orderable) {
    return new QuarantinedObjectSystemNotificationBuilder(
        messageService,
        localeDtoBuilder.build(),
        authenticationHelper.getCurrentUser(),
        NotificationMessageKeys.ORDERABLE_FREE_SYSTEM_NOTIFICATION_TITLE_TEMPLATE,
        NotificationMessageKeys.ORDERABLE_FREE_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE,
        orderable::getFullProductName);
  }

  /**
   * Creates Email Notification Builder for Quarantined Orderable.
   *
   * @param orderable the lot's orderable, not null
   * @return the builder, not null
   */
  public QuarantinedObjectEmailBuilder forQuarantinedOrderableEmail(Orderable orderable) {
    return new QuarantinedObjectEmailBuilder(
        messageService,
        localeDtoBuilder.build(),
        NotificationMessageKeys.ORDERABLE_QUARANTINED_EMAIL_TITLE_TEMPLATE,
        NotificationMessageKeys.ORDERABLE_QUARANTINED_EMAIL_CONTENT_TEMPLATE,
        orderable::getFullProductName);
  }

  /**
   * Creates Email Notification Builder for Free Orderable.
   *
   * @param orderable the lot's orderable, not null
   * @return the builder, not null
   */
  public QuarantinedObjectEmailBuilder forFreeOrderableEmail(Orderable orderable) {
    return new QuarantinedObjectEmailBuilder(
        messageService,
        localeDtoBuilder.build(),
        NotificationMessageKeys.ORDERABLE_FREE_EMAIL_TITLE_TEMPLATE,
        NotificationMessageKeys.ORDERABLE_FREE_EMAIL_CONTENT_TEMPLATE,
        orderable::getFullProductName);
  }
}
