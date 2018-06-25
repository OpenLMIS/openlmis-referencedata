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

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class UserContactDetailsNotificationService {
  private static final String RESOURCE_URL = "/api/userContactDetails";

  @Value("${service.url}")
  private String serviceUrl;

  @Autowired
  private AuthService authService;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Send request to the notification service to add or update user's contact details.
   *
   * @param contactDetails details about user's contact details.
   */
  public void putContactDetails(UserContactDetailsDto contactDetails) {
    restTemplate.postForEntity(
        URI.create(serviceUrl + RESOURCE_URL),
        new HttpEntity<>(contactDetails, createHeadersWithAuth()),
        UserContactDetailsDto.class
    );
  }

  private HttpHeaders createHeadersWithAuth() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + authService.obtainAccessToken());

    return headers;
  }

}
