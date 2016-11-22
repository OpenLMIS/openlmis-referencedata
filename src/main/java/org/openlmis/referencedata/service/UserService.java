package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.ExternalApiException;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.AuthUserRequest;
import org.openlmis.referencedata.util.NotificationRequest;
import org.openlmis.referencedata.util.PasswordChangeRequest;
import org.openlmis.referencedata.util.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ExposedMessageSource messageSource;

  private String virtualHostBaseUrl;

  /**
   * Initialize service object.
   */
  @PostConstruct
  public void init() {
    String virtualHost = Optional.ofNullable(System.getenv("VIRTUAL_HOST")).orElse("localhost");
    virtualHostBaseUrl = "http://" + virtualHost;
  }

  /**
   * Method returns all users with matched parameters.
   *
   * @param username        username of user.
   * @param firstName       firstName of user.
   * @param lastName        lastName of user.
   * @param homeFacility    homeFacility of user.
   * @param active          is the account activated.
   * @param verified        is the account verified.
   * @param loginRestricted is the login restricted.
   * @param extraData       JSON extra data.
   * @return List of users
   */
  public List<User> searchUsers(String username, String firstName, String lastName,
                                Facility homeFacility, Boolean active, Boolean verified,
                                Boolean loginRestricted, String extraData) {

    List<User> foundUsers = null;
    if (username != null || firstName != null || lastName != null || homeFacility != null
        || active != null || verified != null || loginRestricted != null) {
      foundUsers = new ArrayList<>(userRepository.searchUsers(username, firstName, lastName,
          homeFacility, active, verified, loginRestricted));
    }

    if (extraData != null) {
      List<User> extraDataUsers = userRepository.findByExtraData(extraData);

      if (foundUsers != null) {
        // intersection between two lists
        foundUsers.retainAll(extraDataUsers);
      } else {
        foundUsers = extraDataUsers;
      }
    }

    return Optional.ofNullable(foundUsers).orElse(Collections.emptyList());
  }

  /**
   * Creating or updating users.
   */
  @Transactional
  public void save(User user, String token) {
    boolean isNewUser = false;
    if (user.getId() == null) {
      isNewUser = true;
    }
    userRepository.save(user);
    saveAuthUser(user, token);
    if (isNewUser) {
      sendResetPasswordEmail(user, token);
    }
  }

  private void saveAuthUser(User user, String token) {
    AuthUserRequest userRequest = new AuthUserRequest();
    userRequest.setUsername(user.getUsername());
    userRequest.setEmail(user.getEmail());
    userRequest.setReferenceDataUserId(user.getId());

    String url = virtualHostBaseUrl + "/auth/api/users?access_token=" + token;
    RestTemplate restTemplate = new RestTemplate();

    restTemplate.postForObject(url, userRequest, Object.class);
  }

  /**
   * Resets a user's password.
   */
  public void passwordReset(PasswordResetRequest passwordResetRequest, String token) {
    try {
      String url = virtualHostBaseUrl + "/auth/api/users/passwordReset?access_token=" + token;
      RestTemplate restTemplate = new RestTemplate();

      restTemplate.postForObject(url, passwordResetRequest, String.class);

      verifyUser(passwordResetRequest.getUsername());
    } catch (RestClientException ex) {
      throw new ExternalApiException("Could not reset auth user password", ex);
    }
  }

  /**
   * Changes user's password if valid reset token is provided.
   */
  public void changePassword(PasswordChangeRequest passwordChangeRequest, String token) {
    try {
      String url = virtualHostBaseUrl + "/auth/api/users/changePassword?access_token=" + token;

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.postForObject(url, passwordChangeRequest, String.class);

      verifyUser(passwordChangeRequest.getUsername());
    } catch (RestClientException ex) {
      throw new ExternalApiException("Could not change auth user password", ex);
    }
  }

  private void verifyUser(String username) {
    User user = userRepository.findOneByUsername(username);
    user.setVerified(true);
    userRepository.save(user);
  }

  private void sendResetPasswordEmail(User user, String authToken) {
    UUID token = createPasswordResetToken(user.getId(), authToken);

    //TODO: This address needs to be changed when reset password page will be done
    String[] msgArgs = {user.getFirstName(), user.getLastName(),
        user.getUsername(), virtualHostBaseUrl + "reset-password.html" + "/username/"
        + user.getUsername() + "/token/" + token};
    String mailBody = messageSource.getMessage("password.reset.email.body",
        msgArgs, LocaleContextHolder.getLocale());
    String mailSubject = messageSource.getMessage("account.created.email.subject",
        new String[]{}, LocaleContextHolder.getLocale());

    sendMail("notification", user.getEmail(), mailSubject, mailBody, authToken);
  }

  private UUID createPasswordResetToken(UUID userId, String token) {
    try {
      String url = virtualHostBaseUrl + "/auth/api/users/passwordResetToken?userId=" + userId
          + "&access_token=" + token;
      RestTemplate restTemplate = new RestTemplate();

      return restTemplate.postForObject(url, null, UUID.class);
    } catch (RestClientException ex) {
      throw new ExternalApiException("Could not create reset password token", ex);
    }
  }

  private void sendMail(String from, String to, String subject, String content, String token) {
    try {
      NotificationRequest request = new NotificationRequest(from, to, subject, content, null);

      String url = virtualHostBaseUrl + "/notification/notification?access_token=" + token;
      RestTemplate restTemplate = new RestTemplate();

      restTemplate.postForObject(url, request, Object.class);
    } catch (RestClientException ex) {
      throw new ExternalApiException("Could not send reset password email", ex);
    }
  }
}
