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

import static java.util.Objects.isNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.ExternalApiException;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.AuthUserRequest;
import org.openlmis.referencedata.util.messagekeys.SystemMessageKeys;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import org.openlmis.util.NotificationRequest;
import org.openlmis.util.PasswordChangeRequest;
import org.openlmis.util.PasswordResetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  private String baseUrl = System.getenv("BASE_URL");
  static final String MAIL_ADDRESS = Optional.ofNullable(System.getenv("MAIL_ADDRESS"))
      .orElse("noreply@openlmis.org");
  
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Method returns all users with matched parameters.
   *
   * @param queryMap request parameters (username, firstName, lastName, email, homeFacility,
   *                 active, verified, loginRestricted) and JSON extraData.
   * @return List of users
   */
  public List<User> searchUsers(Map<String, Object> queryMap) {

    Map<String, Object> regularQueryMap = new HashMap<>(queryMap);
    Map<String, String> extraData = (Map<String, String>) regularQueryMap.remove("extraData");

    if (queryMap.containsKey("homeFacilityId")) {
      queryMap.put("homeFacility", facilityRepository.findOne(
          (UUID) queryMap.get("homeFacilityId")));
    }
    List<User> foundUsers = new ArrayList<>(userRepository.searchUsers(
        (String) queryMap.get("username"),
        (String) queryMap.get("firstName"),
        (String) queryMap.get("lastName"),
        (String) queryMap.get("email"),
        (Facility) queryMap.get("homeFacility"),
        (Boolean) queryMap.get("active"),
        (Boolean) queryMap.get("verified"),
        (Boolean) queryMap.get("loginRestricted")));

    if (extraData != null && !extraData.isEmpty()) {

      String extraDataString;
      try {
        extraDataString = mapper.writeValueAsString(extraData);
        List<User> extraDataUsers = userRepository.findByExtraData(extraDataString);

        if (foundUsers != null && !foundUsers.isEmpty()) {
          // intersection between two lists
          foundUsers.retainAll(extraDataUsers);
        } else {
          foundUsers = extraDataUsers;
        }
      } catch (JsonProcessingException jpe) {
        LOGGER.debug("Cannot serialize extra data query request body into JSON");
      }
    }

    return Optional.ofNullable(foundUsers).orElse(Collections.emptyList());
  }

  /**
   * Creating or updating users.
   */
  public void save(User user, String token) {
    boolean isNewUser = false;
    if (isNull(user.getId()) || isNull(userRepository.findOne(user.getId()))) {
      isNewUser = true;
    }
    userRepository.save(user);
    saveAuthUser(user, token);
    if (isNewUser) {
      try {
        sendResetPasswordEmail(user, token);
      } catch (ExternalApiException ex) {
        LOGGER.warn("Reset password email could not be send", ex);
      }
    }
  }

  private void saveAuthUser(User user, String token) {
    AuthUserRequest userRequest = new AuthUserRequest();
    userRequest.setUsername(user.getUsername());
    userRequest.setEmail(user.getEmail());
    userRequest.setReferenceDataUserId(user.getId());

    String url = baseUrl + "/api/users/auth?access_token=" + token;
    RestTemplate restTemplate = new RestTemplate();

    restTemplate.postForObject(url, userRequest, Object.class);
  }

  /**
   * Resets a user's password.
   */
  public void passwordReset(PasswordResetRequest passwordResetRequest, String token) {
    try {
      String url = baseUrl + "/api/users/auth/passwordReset?access_token=" + token;
      RestTemplate restTemplate = new RestTemplate();

      restTemplate.postForObject(url, passwordResetRequest, String.class);

      verifyUser(passwordResetRequest.getUsername());
    } catch (RestClientException ex) {
      throw new ExternalApiException(UserMessageKeys.ERROR_EXTERNAL_RESET_PASSWORD_FAILED, ex);
    }
  }

  /**
   * Changes user's password if valid reset token is provided.
   */
  public void changePassword(PasswordChangeRequest passwordChangeRequest, String token) {
    try {
      String url = baseUrl + "/api/users/auth/changePassword?access_token=" + token;

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.postForObject(url, passwordChangeRequest, String.class);

      verifyUser(passwordChangeRequest.getUsername());
    } catch (RestClientException ex) {
      throw new ExternalApiException(
          UserMessageKeys.ERROR_EXTERNAL_CHANGE_PASSWORD_FAILED, ex);
    }
  }

  private void verifyUser(String username) {
    User user = userRepository.findOneByUsername(username);
    user.setVerified(true);
    userRepository.save(user);
  }

  private void sendResetPasswordEmail(User user, String authToken) {
    UUID token = createPasswordResetToken(user.getId(), authToken);

    String[] msgArgs = {user.getFirstName(), user.getLastName(),
        baseUrl + "/#!/resetPassword/" + token};
    String mailBody = messageSource.getMessage(SystemMessageKeys.PASSWORD_RESET_EMAIL_BODY,
        msgArgs, LocaleContextHolder.getLocale());
    String mailSubject = messageSource.getMessage(SystemMessageKeys.ACCOUNT_CREATED_EMAIL_SUBJECT,
        new String[]{}, LocaleContextHolder.getLocale());

    sendMail(MAIL_ADDRESS, user.getEmail(), mailSubject, mailBody, authToken);
  }

  private UUID createPasswordResetToken(UUID userId, String token) {
    try {
      String url = baseUrl + "/api/users/auth/passwordResetToken?userId=" + userId
          + "&access_token=" + token;
      RestTemplate restTemplate = new RestTemplate();

      return restTemplate.postForObject(url, null, UUID.class);
    } catch (RestClientException ex) {
      throw new ExternalApiException(
          UserMessageKeys.ERROR_EXTERNAL_RESET_PASSWORD_CREATE_TOKEN_FAILED, ex);
    }
  }

  private void sendMail(String from, String to, String subject, String content, String token) {
    try {
      NotificationRequest request = new NotificationRequest(from, to, subject, content);

      String url = baseUrl + "/api/notification?access_token=" + token;
      RestTemplate restTemplate = new RestTemplate();

      restTemplate.postForObject(url, request, Object.class);
    } catch (RestClientException ex) {
      throw new ExternalApiException(
          UserMessageKeys.ERROR_EXTERNAL_RESET_PASSWORD_SEND_MESSAGE_FAILED, ex);
    }
  }
}
