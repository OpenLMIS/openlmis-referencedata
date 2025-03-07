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

package org.openlmis.referencedata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.UserApiResponseDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.export.UserImportHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class UserAuthService {

  @Value("${service.url}")
  private String serviceUrl;

  @Autowired
  private AuthService authService;

  private String usersAuthApiUrl = "/api/users/auth";

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Saves auth user details.
   *
   * @param userAuthDetails list of UserAuthDetailsApiContract objects used as a request body
   * @return {@link UserApiResponseDto} as a response object
   */
  public UserApiResponseDto saveUsersAuthDetails(
      List<UserDto.UserAuthDetailsApiContract> userAuthDetails) {
    String url = serviceUrl + usersAuthApiUrl + "/batch";

    ResponseEntity<UserApiResponseDto> response =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            RequestHelper.createEntity(userAuthDetails, authService.obtainAccessToken()),
            new ParameterizedTypeReference<UserApiResponseDto>() {}
        );

    return response.getBody();
  }

  /**
   * Deletes auth users by their ids.
   *
   * @param ids set of user ids (UUIDs)
   */
  public void deleteAuthUsersByUserUuids(Set<UUID> ids) {
    String url = serviceUrl + usersAuthApiUrl + "/batch";

    restTemplate.exchange(
        url,
        HttpMethod.DELETE,
        RequestHelper.createEntity(ids, authService.obtainAccessToken()),
        Void.class
    );
  }

  /**
   * Prepares data from file and calls saving auth user details.
   *
   * @param batch batch with users to be saved
   * @param errors list that stores errors returned from external API call
   * @return list of users for whom auth details were saved successfully
   */
  public List<UserDto> saveUserAuthDetailsFromFile(List<UserDto> batch,
                                             List<ImportResponseDto.ErrorDetails> errors) {
    List<UserDto.UserAuthDetailsApiContract> requestBodyList = new ArrayList<>();
    batch.forEach(userDto -> requestBodyList.add(userDto.toUserAuthDetailsApiContract()));

    UserApiResponseDto response;
    try {
      response = saveUsersAuthDetails(requestBodyList);
    } catch (HttpStatusCodeException ex) {
      errors.add(UserImportHelper.createError(
          "Something went wrong during communication with auth service ", ex));
      return new ArrayList<>();
    }

    UserImportHelper.addErrorsFromResponse(response, errors, batch);

    return UserImportHelper.getSuccessfullyCreatedUsers(batch, response);
  }
}
