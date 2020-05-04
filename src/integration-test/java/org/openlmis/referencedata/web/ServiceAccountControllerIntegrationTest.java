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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.openlmis.referencedata.domain.RightName.SERVICE_ACCOUNTS_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.ServiceAccountMessageKeys.ERROR_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.ServiceAccountMessageKeys.ERROR_TOKEN_MISMATCH;
import static org.openlmis.referencedata.util.messagekeys.SystemMessageKeys.ERROR_UNAUTHORIZED;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.ServiceAccount;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ServiceAccountCreationBody;
import org.openlmis.referencedata.dto.ServiceAccountDto;
import org.openlmis.referencedata.testbuilder.ServiceAccountDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.http.HttpStatus;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ServiceAccountControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/serviceAccounts";
  private static final String TOKEN_URL = RESOURCE_URL + "/{token}";

  private static final String TOKEN = "token";

  private User user = new UserDataBuilder().build();
  private ServiceAccount account = new ServiceAccountDataBuilder().build();
  private ServiceAccountCreationBody body = new ServiceAccountCreationBody(account.getToken());

  private ServiceAccountDto accountDto = new ServiceAccountDto();

  @Before
  @Override
  public void setUp() {
    super.setUp();

    account.export(accountDto);

    given(serviceAccountRepository.save(any(ServiceAccount.class)))
        .willAnswer(invocation -> invocation.getArguments()[0]);
    given(serviceAccountRepository.findById(account.getToken()))
        .willReturn(Optional.of(account));

    given(authenticationHelper.getCurrentUser()).willReturn(user);

    mockUserHasRight(SERVICE_ACCOUNTS_MANAGE);
  }

  @After
  public void tearDown() {
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateServiceAccount() {
    ServiceAccountDto response = post()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .as(ServiceAccountDto.class);

    assertThat(response.getToken(), is(account.getToken()));
    assertThat(response.getCreatedBy(), is(user.getId()));
    assertThat(response.getCreatedDate(), is(notNullValue()));
  }

  @Test
  public void shouldReturnForbiddenForCreateServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = post()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_UNAUTHORIZED));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForCreateServiceAccountEndpoint() {
    startRequest(null)
        .when()
        .body(body)
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  public void shouldRetrieveServiceAccount() {
    ServiceAccountDto response = get()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(ServiceAccountDto.class);

    assertThat(response.getToken(), is(account.getToken()));
    assertThat(response.getCreatedBy(), is(account.getCreationDetails().getCreatedBy()));
  }

  @Test
  public void shouldReturnForbiddenForGetServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = get()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_UNAUTHORIZED));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForGetServiceAccountEndpoint() {
    startRequest(null)
        .pathParam(TOKEN, account.getToken())
        .when()
        .get(TOKEN_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  public void shouldReturnNotFoundIfAccountNotExistForGetServiceAccountEndpoint() {
    given(serviceAccountRepository.findById(account.getToken()))
        .willReturn(Optional.empty());

    String response = get()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_NOT_FOUND));
  }

  @Test
  public void shouldUpdateAccount() {
    ServiceAccount other = new ServiceAccountDataBuilder().build();

    ServiceAccountDto putBody = new ServiceAccountDto();
    other.export(putBody);
    putBody.setToken(account.getToken());

    ServiceAccountDto response = put(putBody)
        .statusCode(HttpStatus.OK.value())
        .extract()
        .as(ServiceAccountDto.class);

    assertThat(response.getToken(), is(account.getToken()));
    assertThat(response.getCreatedBy(), is(account.getCreationDetails().getCreatedBy()));
  }

  @Test
  public void shouldReturnForbiddenForPutServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = put(accountDto)
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_UNAUTHORIZED));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForPutServiceAccountEndpoint() {
    startRequest(null)
        .pathParam(TOKEN, account.getToken())
        .when()
        .body(accountDto)
        .put(TOKEN_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  public void shouldReturnNotFoundIfAccountNotExistForPutServiceAccountEndpoint() {
    given(serviceAccountRepository.findById(account.getToken()))
        .willReturn(Optional.empty());

    String response = put(accountDto)
        .statusCode(HttpStatus.NOT_FOUND.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_NOT_FOUND));
  }

  @Test
  public void shouldReturnBadRequestIfTokenMismatchForPutServiceAccountEndpoint() {
    accountDto.setToken(UUID.randomUUID());

    String response = put(accountDto)
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_TOKEN_MISMATCH));
  }

  @Test
  public void shouldDeleteServiceAccount() {
    delete().statusCode(HttpStatus.NO_CONTENT.value());
  }

  @Test
  public void shouldReturnForbiddenForDeleteServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = delete()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_UNAUTHORIZED));
  }

  @Test
  public void shouldReturnNotFoundIfAccountNotExistForDeleteServiceAccountEndpoint() {
    given(serviceAccountRepository.findById(account.getToken()))
        .willReturn(Optional.empty());

    String response = delete()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(ERROR_NOT_FOUND));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForDeleteServiceAccountEndpoint() {
    startRequest(null)
        .pathParam(TOKEN, account.getToken())
        .when()
        .delete(TOKEN_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  private ValidatableResponse post() {
    return startRequest(getTokenHeader())
        .when()
        .body(body)
        .post(RESOURCE_URL)
        .then();
  }

  private ValidatableResponse get() {
    return startRequest(getTokenHeader())
        .pathParam(TOKEN, account.getToken())
        .when()
        .get(TOKEN_URL)
        .then();
  }

  private ValidatableResponse put(ServiceAccountDto putBody) {
    return startRequest(getTokenHeader())
        .pathParam(TOKEN, account.getToken())
        .when()
        .body(putBody)
        .put(TOKEN_URL)
        .then();
  }

  private ValidatableResponse delete() {
    return startRequest(getTokenHeader())
        .pathParam(TOKEN, account.getToken())
        .when()
        .delete(TOKEN_URL)
        .then();
  }
}
