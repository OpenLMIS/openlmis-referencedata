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

package org.openlmis.referencedata.service.notification;

import static org.openlmis.referencedata.service.notification.NotificationChannelDto.EMAIL;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import org.openlmis.referencedata.service.AuthService;
import org.openlmis.referencedata.util.RequestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {
  private static final String NOTIFICATIONS_API_PATH = "/api/notifications";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired private AuthService authService;

  @Value("${notification.url}")
  private String notificationBaseUrl;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Send an email notification.
   *
   * @param recipientId receiver's ID
   * @param subject subject of the email
   * @param content content of the email
   * @return true if success, false if failed.
   */
  @Async("singleThreadExecutor")
  public Future<Boolean> notifyAsyncEmail(UUID recipientId, String subject, String content) {
    NotificationDto request = buildNotification(recipientId, subject, content);
    String url = notificationBaseUrl + NOTIFICATIONS_API_PATH;

    try {
      restTemplate.postForObject(
          RequestHelper.createUri(url),
          RequestHelper.createEntity(request, authService.obtainAccessToken()),
          Object.class);
      return new AsyncResult<>(true);
    } catch (HttpStatusCodeException ex) {
      logger.error(
          "Unable to send notification. Error code: {}, response message: {}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      return new AsyncResult<>(false);
    }
  }

  private NotificationDto buildNotification(UUID recipientId, String subject, String content) {
    Map<String, MessageDto> messages = new HashMap<>();
    messages.put(EMAIL.toString(), new MessageDto(subject, content));

    return new NotificationDto(recipientId, messages);
  }
}
