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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.openlmis.referencedata.domain.CustomPageImpl;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.SaveBatchResultDto;
import org.openlmis.referencedata.dto.UserApiResponseDto;
import org.openlmis.referencedata.dto.UserContactDetailsDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.export.UserImportHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class UserDetailsService {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserImportHelper userImportHelper;

  @Value("${service.url}")
  private String serviceUrl;

  private String userContactDetailsApiUrl = "/api/userContactDetails";

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Gets all user contact detail instances.
   *
   * @return page of UserContactDetailsApiContract object
   */
  public PageImpl<UserContactDetailsDto.UserContactDetailsApiContract> getUserContactDetails() {
    String url = serviceUrl + userContactDetailsApiUrl;
    ResponseEntity<CustomPageImpl<UserContactDetailsDto.UserContactDetailsApiContract>> response =
        restTemplate.exchange(
            url,
            HttpMethod.GET,
            RequestHelper.createEntity(
                RequestHelper.createHeadersWithAuth(authService.obtainAccessToken())),
            new ParameterizedTypeReference
                <CustomPageImpl<UserContactDetailsDto.UserContactDetailsApiContract>>() {}
        );

    return response.getBody();
  }

  /**
   * Saves multiple user contact details.
   *
   * @param userDetails list of UserContactDetailsApiContract objects as a request body
   * @return {@link UserApiResponseDto} as a response object
   */
  public UserApiResponseDto saveUsersContactDetails(
      List<UserContactDetailsDto.UserContactDetailsApiContract> userDetails) {
    String url = serviceUrl + userContactDetailsApiUrl + "/batch";

    ResponseEntity<UserApiResponseDto> response =
        restTemplate.exchange(
            url,
            HttpMethod.PUT,
            RequestHelper.createEntity(userDetails, authService.obtainAccessToken()),
            new ParameterizedTypeReference<UserApiResponseDto>() {},
            userDetails
        );

    return response.getBody();
  }

  /**
   * Deletes user contact details by their ids.
   *
   * @param ids set of user ids (UUIDs)
   */
  public void deleteUserContactDetailsByUserUuids(Set<UUID> ids) {
    String url = serviceUrl + userContactDetailsApiUrl + "/batch";

    restTemplate.exchange(
        url,
        HttpMethod.DELETE,
        RequestHelper.createEntity(ids, authService.obtainAccessToken()),
        Void.class
    );
  }

  /**
   * Prepares data from file and calls saving contact user details.
   *
   * @param batch batch with users to be saved
   * @param importedDtos list with imported data from file
   * @return {@link SaveBatchResultDto} object with import batch results
   */
  public SaveBatchResultDto<UserDto> saveUsersContactDetailsFromFile(List<UserDto> batch,
                                                                     List<UserDto> importedDtos) {
    List<UserContactDetailsDto.UserContactDetailsApiContract> requestBodyList = new ArrayList<>();
    for (UserDto userDto : batch) {
      Optional<UserDto> userWithDataFromFile = importedDtos.stream()
          .filter(dto -> dto.getUsername().equalsIgnoreCase(userDto.getUsername()))
          .findFirst();
      if (userWithDataFromFile.isPresent()) {
        UserContactDetailsDto.UserContactDetailsApiContract requestBody =
            userWithDataFromFile.get().toUserContactDetailsApiContract();
        requestBody.setReferenceDataUserId(userDto.getId());
        requestBodyList.add(requestBody);
      }
    }

    List<ImportResponseDto.ErrorDetails> errorList = new ArrayList<>();
    UserApiResponseDto response;
    try {
      response = saveUsersContactDetails(requestBodyList);
    } catch (HttpStatusCodeException ex) {
      errorList.add(UserImportHelper.createError(
          "Something went wrong during communication with notification service ", ex));
      return new SaveBatchResultDto<>(Collections.emptyList(), errorList);
    }

    List<UserDto> successfulEntries = userImportHelper.getSuccessfullyCreatedUsers(batch, response);
    errorList = userImportHelper.collectErrorsFromResponse(response, batch);

    return new SaveBatchResultDto<>(successfulEntries, errorList);
  }
}
