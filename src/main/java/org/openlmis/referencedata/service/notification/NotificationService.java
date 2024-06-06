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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.service.AuthService;
import org.openlmis.referencedata.util.RequestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired private AuthService authService;

  @Value("${notification.url}")
  private String notificationUrl;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Send an email notification.
   *
   * @param user receiver of the notification
   * @param subject subject of the email
   * @param content content of the email
   * @return true if success, false if failed.
   */
  @Async
  public Future<Boolean> notifyAsyncEmail(User user, String subject, String content) {
    CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
    NotificationDto request = buildNotification(user, subject, content);
    String url = notificationUrl + "/api/notifications";

    try {
      restTemplate.postForObject(
          RequestHelper.createUri(url),
          RequestHelper.createEntity(request, authService.obtainAccessToken()),
          Object.class);
      resultFuture.complete(true);
    } catch (HttpStatusCodeException ex) {
      logger.error(
          "Unable to send notification. Error code: {}, response message: {}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      resultFuture.complete(false);
    }
    return resultFuture;
  }

  private NotificationDto buildNotification(User user, String subject, String content) {
    Map<String, MessageDto> messages = new HashMap<>();
    messages.put(EMAIL.toString(), new MessageDto(subject, content));

    return new NotificationDto(user.getId(), messages);
  }
}
