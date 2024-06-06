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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.i18n.MessageService;

public class QuarantinedObjectSystemNotificationBuilder {
  private static final String DATE_CTX_KEY = "date";
  private static final String OBJECT_NAME_CTX_KEY = "objectName";

  private final User author;
  private final String titleTemplate;
  private final String contentTemplate;
  private final StringSubstitutor templateSubstitutor;

  /**
   * Creates new instance of the Builder.
   *
   * @param messageService the translation provider, not null
   * @param author the notification's author, not null
   * @param valuesProvider the replacement value provider, not null
   */
  QuarantinedObjectSystemNotificationBuilder(
      MessageService messageService,
      User author,
      String titleTemplateKey,
      String contentTemplateKey,
      ValuesProvider valuesProvider) {
    this.author = author;
    this.titleTemplate = messageService.localizeString(titleTemplateKey);
    this.contentTemplate = messageService.localizeString(contentTemplateKey);
    this.templateSubstitutor = new StringSubstitutor(newContext(valuesProvider));
  }

  private static Map<String, String> newContext(ValuesProvider valuesProvider) {
    final Map<String, String> context = new HashMap<>();
    context.put(DATE_CTX_KEY, ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    context.put(OBJECT_NAME_CTX_KEY, valuesProvider.getObjectIdentifier());
    return context;
  }

  /**
   * Builds new instance of SystemNotification.
   *
   * @return a new insatnce of SystemNotification, never null
   */
  public SystemNotification buildSystemNotification() {
    final SystemNotification systemNotification = new SystemNotification();
    systemNotification.setAuthor(author);
    systemNotification.setTitle(templateSubstitutor.replace(titleTemplate));
    systemNotification.setMessage(templateSubstitutor.replace(contentTemplate));
    systemNotification.setActive(true);
    return systemNotification;
  }

  /** Provides values for placeholder replacement in the message content. */
  public interface ValuesProvider {
    String getObjectIdentifier();
  }
}
