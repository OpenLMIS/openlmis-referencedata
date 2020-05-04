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

package org.openlmis.referencedata.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.SystemNotificationDto;
import org.openlmis.referencedata.dto.UserObjectReferenceDto;
import org.openlmis.referencedata.repository.custom.SystemNotificationRepositoryCustom;
import org.openlmis.referencedata.testbuilder.SystemNotificationDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.SystemNotificationMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ValidationMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class SystemNotificationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String AUTHOR_ID = "authorId";
  private static final String IS_DISPLAYED = "isDisplayed";

  private static final String RESOURCE_PATH = SystemNotificationController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_PATH + SystemNotificationController.ID_URL;

  private User author;
  private SystemNotification notification;
  private SystemNotification notification2;
  private SystemNotificationDto notificationDto = new SystemNotificationDto();
  private Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);

  @Override
  @Before
  public void setUp() {
    super.setUp();

    author = new UserDataBuilder().build();
    notification = new SystemNotificationDataBuilder().withAuthor(author).build();
    notification2 = new SystemNotificationDataBuilder().withAuthor(author).build();
    pageable = PageRequest.of(0, 10);

    notificationDto.setServiceUrl(baseUri);
    notification.export(notificationDto);

    given(systemNotificationRepository.save(any(SystemNotification.class)))
        .willAnswer(new SaveAnswer<>());

    mockUserHasRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);
  }

  @Test
  public void searchShouldReturnBadRequestOnException() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(AUTHOR_ID, notification.getAuthor().getId());
    parameters.put(IS_DISPLAYED, true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .params(parameters)
        .param("some-unknown-parameter", "some-value")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSystemNotificationsByParamsWithoutCheckingRights() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put(AUTHOR_ID, notification.getAuthor().getId());
    parameters.put(IS_DISPLAYED, true);

    given(systemNotificationRepository.search(
        any(SystemNotificationRepositoryCustom.SearchParams.class), eq(pageable)))
        .willReturn(Pagination.getPage(Arrays.asList(notification, notification2),
            PageRequest.of(0, 10)));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .params(parameters)
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(2));

    assertResponseBody(response, "content[0]", is(notification.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSystemNotificationsWithoutCheckingRights() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    given(systemNotificationRepository.search(
        any(SystemNotificationRepositoryCustom.SearchParams.class), eq(pageable)))
        .willReturn(Pagination.getPage(Arrays.asList(notification, notification2),
            PageRequest.of(0, 10)));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(2));

    assertResponseBody(response, "content[0]", is(notification.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSystemNotificationsWithExpand() {

    Map<String, Object> parameters = new HashMap<>();
    parameters.put(AUTHOR_ID, notification.getAuthor().getId());
    parameters.put(IS_DISPLAYED, true);

    given(systemNotificationRepository.search(
        any(SystemNotificationRepositoryCustom.SearchParams.class), eq(pageable)))
        .willReturn(Pagination.getPage(Arrays.asList(notification), pageable, 1));

    parameters.put("expand", "author");

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PAGE, pageable.getPageNumber())
        .queryParam(SIZE, pageable.getPageSize())
        .params(parameters)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1));

    assertResponseBodyWithExpand(response, "content[0]", Arrays.asList(
        is(notification.getAuthor().getFirstName()),
        is(notification.getAuthor().getLastName())));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSystemNotification() {
    given(systemNotificationRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(notification));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, notification.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK);

    assertResponseBody(response, "", is(notification.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInGetSystemNotification() {
    restAssured
        .given()
        .pathParam(ID, notification.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInGetSystemNotification() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, notification.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfSystemNotificationDoesNotExistInGetSystemNotification() {
    given(systemNotificationRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, notification.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(SystemNotificationMessageKeys.ERROR_NOT_FOUND_WITH_ID));


    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSystemNotification() {
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(author));
    notificationDto.setId(null);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(notificationDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_CREATED);

    assertResponseBody(response, "", is(notNullValue(String.class)));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequiredFieldIsMissedInPostSystemNotifications() {
    given(systemNotificationRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    notificationDto.setId(null);
    notificationDto.setAuthor(new UserObjectReferenceDto());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(notificationDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(SystemNotificationMessageKeys.ERROR_AUTHOR_REQUIRED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequestBodyIsInvalidInPostSystemNotifications() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(notificationDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(SystemNotificationMessageKeys.ERROR_ID_PROVIDED));

    // we don't check request body because of the purpose of the test
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.validates());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPostSystemNotifications() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(notificationDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInPostSystemNotifications() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(notificationDto)
        .when()
        .post(RESOURCE_PATH)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSystemNotification() {
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(author));
    notificationDto.setStartDate(ZonedDateTime.now().plusDays(1));

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, notificationDto.getId())
        .body(notificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK);

    assertResponseBody(response, "", is(notification.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfUneditableFieldIsChangedInPutSystemNotification() {
    given(systemNotificationRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(notification));
    notificationDto.setCreatedDate(ZonedDateTime.now().plusDays(1));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, notificationDto.getId())
        .body(notificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(ValidationMessageKeys.ERROR_IS_INVARIANT));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSystemNotificationInPutIfDoesNotExist() {
    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(author));
    UUID newNotificationId = UUID.randomUUID();
    notificationDto.setId(newNotificationId);

    ValidatableResponse response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, newNotificationId)
        .body(notificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK);

    assertResponseBody(response, "", is(newNotificationId.toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfRequestIsInvalidInPutSystemNotification() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, UUID.randomUUID())
        .body(notificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(SystemNotificationMessageKeys.ERROR_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInPutSystemNotification() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, notificationDto.getId())
        .body(notificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInPutSystemNotification() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, notificationDto.getId())
        .body(notificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteSystemNotification() {
    given(systemNotificationRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(notification));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", notification.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfTokenWasNotProvidedInDeleteSystemNotification() {
    restAssured
        .given()
        .pathParam(ID, notification.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInDeleteSystemNotification() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, notification.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN)
        .body(MESSAGE_KEY, is(MESSAGEKEY_ERROR_UNAUTHORIZED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfSystemNotificationDoesNotExistInDeleteSystemNotification() {
    given(systemNotificationRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, notification.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(SystemNotificationMessageKeys.ERROR_NOT_FOUND_WITH_ID));


    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAuditLog() {
    given(systemNotificationRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(notification));

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_PATH);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsInGetAuditLog() {
    mockUserHasNoRight(RightName.SYSTEM_NOTIFICATIONS_MANAGE);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_PATH);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfSystemNotificationDoesNotExistInGetAuditLog() {
    given(systemNotificationRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_PATH);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void assertResponseBody(ValidatableResponse response, String resourcePath,
      Matcher<String> idMatcher) {
    response
        .rootPath(resourcePath)
        .body(ID, idMatcher)
        .body("title", is(notification.getTitle()))
        .body("message", is(notification.getMessage()))
        .body("active", is(notification.isActive()))
        .body("author.id", is(notification.getAuthor().getId().toString()));
  }

  private void assertResponseBodyWithExpand(ValidatableResponse response, String resourcePath,
      List<Matcher<String>> idMatchers) {
    response
        .rootPath(resourcePath)
        .body("author.firstName", idMatchers.get(0))
        .body("author.lastName", idMatchers.get(1));
  }

}
