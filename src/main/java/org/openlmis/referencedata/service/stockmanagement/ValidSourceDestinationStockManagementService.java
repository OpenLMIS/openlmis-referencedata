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

package org.openlmis.referencedata.service.stockmanagement;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.AuthService;
import org.openlmis.referencedata.util.RequestHelper;
import org.openlmis.referencedata.util.messagekeys.stockmanagement.ValidAssignmentMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class ValidSourceDestinationStockManagementService {

  private static final String VALID_DESTINATIONS_API_PATH = "/api/validDestinations";
  private static final String VALID_SOURCES_API_PATH = "/api/validSources";

  @Autowired
  private AuthService authService;

  @Value("${stockmanagement.url}")
  private String stockManagementBaseUrl;

  private RestOperations restTemplate = new RestTemplate();

  public void createDestination(ValidAssignmentDto destinationDto) {
    createAssignment(destinationDto, VALID_DESTINATIONS_API_PATH);
  }

  public void createSource(ValidAssignmentDto sourceDto) {
    createAssignment(sourceDto, VALID_SOURCES_API_PATH);
  }

  private void createAssignment(ValidAssignmentDto assignmentDto, String apiPath) {
    String url = stockManagementBaseUrl + apiPath;

    try {
      restTemplate.postForObject(
          RequestHelper.createUri(url),
          RequestHelper.createEntity(assignmentDto, authService.obtainAccessToken()),
          Object.class
      );
    } catch (HttpStatusCodeException ex) {
      throw new ValidationMessageException(
          ValidAssignmentMessageKeys.ERROR_COULD_NOT_CREATE_ASSIGNMENT,
          assignmentDto.getProgramId(),
          assignmentDto.getFacilityTypeId(),
          assignmentDto.getNode().getReferenceId(), ex);
    }
  }

}

