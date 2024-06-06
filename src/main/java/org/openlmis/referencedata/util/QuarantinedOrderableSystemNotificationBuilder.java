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

import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.i18n.MessageService;
import org.openlmis.referencedata.util.messagekeys.NotificationMessageKeys;

public class QuarantinedOrderableSystemNotificationBuilder
    extends QuarantinedObjectSystemNotificationBuilder {
  /**
   * Creates new instance of the Builder.
   *
   * @param messageService the translation provider, not null
   * @param author the notification's author, not null
   * @param orderable the orderable, not null
   */
  public QuarantinedOrderableSystemNotificationBuilder(
      MessageService messageService, User author, Orderable orderable) {
    super(
        messageService,
        author,
        NotificationMessageKeys.ORDERABLE_QUARANTINED_SYSTEM_NOTIFICATION_TITLE_TEMPLATE,
        NotificationMessageKeys.ORDERABLE_QUARANTINED_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE,
        orderable::getFullProductName);
  }
}
