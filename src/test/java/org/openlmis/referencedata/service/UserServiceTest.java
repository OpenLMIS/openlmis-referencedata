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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.service.UserService.MAIL_ADDRESS;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.AuthUserRequest;
import org.openlmis.util.NotificationRequest;
import org.openlmis.util.PasswordChangeRequest;
import org.openlmis.util.PasswordResetRequest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


@SuppressWarnings("PMD.TooManyMethods")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
@PrepareForTest({UserService.class})
public class UserServiceTest {

  private static final String AUTH_TOKEN = "authToken";
  private static final String EXTRA_DATA_VALUE = "extraDataValue";
  private static final String FIRST_NAME_SEARCH = "FirstNameMatchesTwoUsers";
  private static final String EXTRA_DATA_KEY = "extraDataKey";
  private static final String EXTRA_DATA_PROP_NAME = "extraData";

  @Mock
  private UserRepository userRepository;

  @Mock
  private ExposedMessageSource messageSource;

  @InjectMocks
  private UserService userService;

  private User user;
  private User user2;
  private User user3;
  private User user4;

  private Map<String, Object> userSearch;
  private Map<String, String> extraData;
  private ObjectMapper mapper = new ObjectMapper();
  private String extraDataString;
  private Map<String, Object> queryMap;

  @Before
  public void setUp() throws JsonProcessingException {
    user = generateUser();
    user2 = mock(User.class);
    user3 = mock(User.class);
    user4 = mock(User.class);
    userSearch = Collections.singletonMap("firstName", FIRST_NAME_SEARCH);
    extraData = Collections.singletonMap(EXTRA_DATA_KEY, EXTRA_DATA_VALUE);
    extraDataString = mapper.writeValueAsString(extraData);
    queryMap = new HashMap<>();
  }

  @Test
  @Ignore
  public void searchUsersShouldGetNoUsersIfSearchesGetDisjointedResults() {
    when(userRepository
        .searchUsers(
            any(String.class),
            eq(FIRST_NAME_SEARCH),
            any(String.class),
            any(String.class),
            any(Facility.class),
            any(Boolean.class),
            any(Boolean.class),
            any(Boolean.class)))
        .thenReturn(Arrays.asList(user, user2));

    when(userRepository.findByExtraData(extraDataString))
        .thenReturn(Arrays.asList(user3, user4));

    queryMap.putAll(userSearch);
    queryMap.put(EXTRA_DATA_PROP_NAME, extraData);

    List<User> receivedUsers = userService.searchUsers(queryMap);

    assertEquals(0, receivedUsers.size());
  }

  @Test
  @Ignore
  public void searchUsersShouldGetSomeUsersForOverlappingSearchResults() {
    when(userRepository
        .searchUsers(
            any(String.class),
            eq(FIRST_NAME_SEARCH),
            any(String.class),
            any(String.class),
            any(Facility.class),
            any(Boolean.class),
            any(Boolean.class),
            any(Boolean.class)))
        .thenReturn(Arrays.asList(user, user2));

    when(userRepository.findByExtraData(extraDataString))
        .thenReturn(Arrays.asList(user2, user3));

    queryMap.putAll(userSearch);
    queryMap.put(EXTRA_DATA_PROP_NAME, extraData);

    List<User> receivedUsers = userService.searchUsers(queryMap);

    assertEquals(1, receivedUsers.size());
    assertEquals(user2, receivedUsers.get(0));
  }

  @Test
  public void searchUsersShouldGetAllUsersIfSearchResultsAreTheSame() {
    when(userRepository
        .searchUsers(
            any(String.class),
            eq(FIRST_NAME_SEARCH),
            any(String.class),
            any(String.class),
            any(Facility.class),
            any(Boolean.class),
            any(Boolean.class),
            any(Boolean.class)))
        .thenReturn(Arrays.asList(user, user2));

    when(userRepository.findByExtraData(extraDataString))
        .thenReturn(Arrays.asList(user, user2));

    queryMap.putAll(userSearch);
    queryMap.put(EXTRA_DATA_PROP_NAME, extraData);

    List<User> receivedUsers = userService.searchUsers(queryMap);

    assertEquals(2, receivedUsers.size());
    assertTrue(receivedUsers.contains(user));
    assertTrue(receivedUsers.contains(user2));
  }

  /*@Test
  public void searchUsersShouldNotDoRegularSearchIfNoParameters() {
    when(userRepository.findByExtraData(extraDataString))
        .thenReturn(Arrays.asList(user, user2));

    queryMap.put(EXTRA_DATA_PROP_NAME, extraData);

    List<User> receivedUsers = userService.searchUsers(queryMap);

    assertEquals(2, receivedUsers.size());
    assertTrue(receivedUsers.contains(user));
    assertTrue(receivedUsers.contains(user2));
    verify(userRepository, never()).searchUsers(
        any(String.class),
        any(String.class),
        any(String.class),
        any(String.class),
        any(Facility.class),
        any(Boolean.class),
        any(Boolean.class),
        any(Boolean.class));
  }*/

  @Test
  public void searchUsersShouldNotSearchExtraDataIfParameterIsNullOrEmpty() {
    when(userRepository
        .searchUsers(
            any(String.class),
            eq(FIRST_NAME_SEARCH),
            any(String.class),
            any(String.class),
            any(Facility.class),
            any(Boolean.class),
            any(Boolean.class),
            any(Boolean.class)))
        .thenReturn(Arrays.asList(user, user2));

    queryMap.putAll(userSearch);

    List<User> receivedUsers = userService.searchUsers(queryMap);

    assertEquals(2, receivedUsers.size());
    assertTrue(receivedUsers.contains(user));
    assertTrue(receivedUsers.contains(user2));
    verify(userRepository, never()).findByExtraData(any(String.class));
  }

  @Test
  public void shouldSaveRequisitionAndAuthUsers() throws Exception {
    when(userRepository.save(user)).thenReturn(user);
    when(userRepository.findOne(user.getId())).thenReturn(user);

    RestTemplate restTemplate = mock(RestTemplate.class);
    whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

    userService.save(user, AUTH_TOKEN);

    verify(userRepository).save(user);

    ArgumentCaptor<AuthUserRequest> authUserCaptor = ArgumentCaptor.forClass(AuthUserRequest.class);
    verify(restTemplate).postForObject(contains(AUTH_TOKEN), authUserCaptor.capture(), any());

    assertEquals(1, authUserCaptor.getAllValues().size());
    AuthUserRequest authUser = authUserCaptor.getValue();

    assertEquals(user.getUsername(), authUser.getUsername());
    assertEquals(user.getId(), authUser.getReferenceDataUserId());
    assertEquals(user.getEmail(), authUser.getEmail());
    assertTrue(authUser.getEnabled());
    assertEquals("USER", authUser.getRole());
  }

  @Test
  public void shouldSendResetPasswordEmailWhenNewUserIsCreated() throws Exception {
    user.setId(null);
    UUID resetPasswordTokenId = UUID.randomUUID();
    String mailSubject = "subject";
    String mailBody = "body";

    when(userRepository.save(user)).thenReturn(user);

    RestTemplate restTemplate = mock(RestTemplate.class);
    whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

    when(restTemplate.postForObject(contains("passwordResetToken?userId=" + user.getId()),
        any(), eq(UUID.class))).thenReturn(resetPasswordTokenId);

    when(messageSource.getMessage(contains(mailSubject), any(Object[].class),
        any(Locale.class))).thenReturn(mailSubject);

    when(messageSource.getMessage(contains(mailBody), any(Object[].class),
        any(Locale.class))).thenReturn(mailBody);

    userService.save(user, AUTH_TOKEN);

    verify(userRepository).save(user);

    verify(restTemplate).postForObject(anyString(), isA(AuthUserRequest.class), eq(Object.class));

    NotificationRequest request = new NotificationRequest(MAIL_ADDRESS, user.getEmail(),
        mailSubject, mailBody);

    verify(restTemplate).postForObject(contains("notification?access_token=" + AUTH_TOKEN),
        refEq(request), eq(Object.class));
  }

  @Test
  public void shouldNotSendResetPasswordEmailWhenUserIsUpdated() throws Exception {
    when(userRepository.save(user)).thenReturn(user);
    when(userRepository.findOne(user.getId())).thenReturn(user);

    RestTemplate restTemplate = mock(RestTemplate.class);
    whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

    userService.save(user, AUTH_TOKEN);

    verify(userRepository).save(user);

    verify(restTemplate).postForObject(contains(AUTH_TOKEN),
        isA(AuthUserRequest.class), eq(Object.class));

    verify(restTemplate, never()).postForObject(contains("passwordResetToken"),
        any(), eq(UUID.class));

    verify(restTemplate, never()).postForObject(contains("notification"),
        any(), eq(Object.class));
  }

  @Test
  public void shouldResetPasswordAndVerifyUser() throws Exception {
    PasswordResetRequest passwordResetRequest = new PasswordResetRequest("username", "newPassword");

    when(userRepository.findOneByUsername(passwordResetRequest.getUsername())).thenReturn(user);

    RestTemplate restTemplate = mock(RestTemplate.class);
    whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

    assertFalse(user.isVerified());

    userService.passwordReset(passwordResetRequest, AUTH_TOKEN);

    verify(userRepository).save(user);

    assertTrue(user.isVerified());

    verify(restTemplate).postForObject(contains("passwordReset?access_token=" + AUTH_TOKEN),
        refEq(passwordResetRequest), eq(String.class));
  }

  @Test
  public void shouldChangePasswordAndVerifyUser() throws Exception {
    PasswordChangeRequest passwordResetRequest = new PasswordChangeRequest(UUID.randomUUID(),
        "username", "newPassword");

    when(userRepository.findOneByUsername(passwordResetRequest.getUsername())).thenReturn(user);

    RestTemplate restTemplate = mock(RestTemplate.class);
    whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

    assertFalse(user.isVerified());

    userService.changePassword(passwordResetRequest, AUTH_TOKEN);

    verify(userRepository).save(user);

    assertTrue(user.isVerified());

    verify(restTemplate).postForObject(contains("changePassword?access_token=" + AUTH_TOKEN),
        refEq(passwordResetRequest), eq(String.class));
  }

  private User generateUser() {
    return new UserBuilder("kota", "Ala", "ma", "test@mail.com")
        .setId(UUID.randomUUID())
        .setTimezone("UTC")
        .setHomeFacility(mock(Facility.class))
        .setVerified(false)
        .setActive(true)
        .setLoginRestricted(true)
        .setAllowNotify(true)
        .setExtraData(Collections.singletonMap(EXTRA_DATA_KEY, EXTRA_DATA_VALUE))
        .createUser();
  }
}
