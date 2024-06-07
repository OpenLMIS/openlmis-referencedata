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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.text.StringSubstitutor;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.i18n.MessageService;

public class QuarantinedObjectEmailBuilder {
  private static final String DATE_CTX_KEY = "date";
  private static final String OBJECT_NAME_CTX_KEY = "objectName";
  private static final String USERNAME_CTX_KEY = "username";

  private final String titleTemplate;
  private final String contentTemplate;
  private final Map<String, String> templateContext;

  /**
   * Creates new instance of the Builder.
   *
   * @param messageService the translation provider, not null
   * @param valuesProvider the replacement value provider, not null
   */
  QuarantinedObjectEmailBuilder(
      MessageService messageService,
      String titleTemplateKey,
      String contentTemplateKey,
      ValuesProvider valuesProvider) {
    this.titleTemplate = messageService.localizeString(titleTemplateKey);
    this.contentTemplate = messageService.localizeString(contentTemplateKey);
    this.templateContext = newContext(valuesProvider);
  }

  private static Map<String, String> newContext(ValuesProvider valuesProvider) {
    final Map<String, String> context = new HashMap<>();
    context.put(DATE_CTX_KEY, ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    context.put(OBJECT_NAME_CTX_KEY, valuesProvider.getObjectIdentifier());
    return context;
  }

  public Email buildEmail(User user) {
    final StringSubstitutor sub = createStringSubstitutor(user);
    return new Email(sub.replace(titleTemplate), sub.replace(contentTemplate));
  }

  private StringSubstitutor createStringSubstitutor(User user) {
    final Map<String, String> userContext = new HashMap<>(templateContext);
    userContext.put(USERNAME_CTX_KEY, user.getUsername());
    return new StringSubstitutor(userContext);
  }

  /** Provides values for placeholder replacement in the message content. */
  public interface ValuesProvider {
    String getObjectIdentifier();
  }

  @AllArgsConstructor
  @Getter
  public static final class Email {
    private final String title;
    private final String content;
  }
}
