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

package org.openlmis.referencedata.util.messagekeys;

public abstract class NotificationMessageKeys {
  public static final String LOT_QUARANTINED_EMAIL_TITLE_TEMPLATE =
      "referencedata-angola.email.quarantinedLot.subject.template";
  public static final String LOT_QUARANTINED_EMAIL_CONTENT_TEMPLATE =
      "referencedata-angola.email.quarantinedLot.content.template";
  public static final String ORDERABLE_QUARANTINED_EMAIL_TITLE_TEMPLATE =
      "referencedata-angola.email.quarantinedOrderable.subject.template";
  public static final String ORDERABLE_QUARANTINED_EMAIL_CONTENT_TEMPLATE =
      "referencedata-angola.email.quarantinedOrderable.content.template";

  public static final String LOT_QUARANTINED_SYSTEM_NOTIFICATION_TITLE_TEMPLATE =
      "referencedata-angola.systemNotification.quarantinedLot.subject.template";
  public static final String LOT_QUARANTINED_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE =
      "referencedata-angola.systemNotification.quarantinedLot.content.template";
  public static final String ORDERABLE_QUARANTINED_SYSTEM_NOTIFICATION_TITLE_TEMPLATE =
      "referencedata-angola.systemNotification.quarantinedOrderable.subject.template";
  public static final String ORDERABLE_QUARANTINED_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE =
      "referencedata-angola.systemNotification.quarantinedOrderable.content.template";

  public static final String LOT_FREE_EMAIL_TITLE_TEMPLATE =
      "referencedata-angola.email.freeLot.subject.template";
  public static final String LOT_FREE_EMAIL_CONTENT_TEMPLATE =
      "referencedata-angola.email.freeLot.content.template";
  public static final String ORDERABLE_FREE_EMAIL_TITLE_TEMPLATE =
      "referencedata-angola.email.freeOrderable.subject.template";
  public static final String ORDERABLE_FREE_EMAIL_CONTENT_TEMPLATE =
      "referencedata-angola.email.freeOrderable.content.template";

  public static final String LOT_FREE_SYSTEM_NOTIFICATION_TITLE_TEMPLATE =
      "referencedata-angola.systemNotification.freeLot.subject.template";
  public static final String LOT_FREE_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE =
      "referencedata-angola.systemNotification.freeLot.content.template";
  public static final String ORDERABLE_FREE_SYSTEM_NOTIFICATION_TITLE_TEMPLATE =
      "referencedata-angola.systemNotification.freeOrderable.subject.template";
  public static final String ORDERABLE_FREE_SYSTEM_NOTIFICATION_CONTENT_TEMPLATE =
      "referencedata-angola.systemNotification.freeOrderable.content.template";

  protected NotificationMessageKeys() {
    throw new UnsupportedOperationException();
  }
}
