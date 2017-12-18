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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.openlmis.referencedata.domain.RightName.SERVICE_ACCOUNTS_MANAGE;
import static org.openlmis.referencedata.util.messagekeys.ServiceAccountMessageKeys.ERROR_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.SystemMessageKeys.ERROR_UNAUTHORIZED;

import com.jayway.restassured.response.ValidatableResponse;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.ServiceAccount;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ServiceAccountDto;
import org.openlmis.referencedata.service.AuthService;
import org.openlmis.referencedata.testbuilder.ServiceAccountDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ServiceAccountControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/serviceAccounts";
  private static final String ID_URL = RESOURCE_URL + "/{apiKey}";
  private static final String API_KEY = "apiKey";

  @MockBean
  private AuthService authService;

  private ServiceAccount account;
  private ServiceAccountDto accountDto;

  private User user = new UserDataBuilder().build();

  private UUID apiKey = UUID.randomUUID();

  @Before
  @Override
  public void setUp() {
    super.setUp();

    accountDto = new ServiceAccountDto();

    account = new ServiceAccountDataBuilder().build();
    account.export(accountDto);

    given(serviceAccountRepository.save(any(ServiceAccount.class)))
        .willAnswer(invocation -> invocation.getArguments()[0]);

    given(authenticationHelper.getCurrentUser()).willReturn(user);
    given(authService.createApiKey()).willReturn(apiKey);

    mockUserHasRight(SERVICE_ACCOUNTS_MANAGE);
  }

  @Test
  public void shouldCreateServiceAccount() {
    ServiceAccountDto response = post()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .as(ServiceAccountDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    assertThat(response.getApiKey(), is(equalTo(apiKey)));
    assertThat(response.getCreatedBy(), is(equalTo(user.getId())));
    assertThat(response.getCreatedDate(), is(notNullValue()));

    verify(authenticationHelper).getCurrentUser();
    verify(authService).createApiKey();
  }

  @Test
  public void shouldReturnForbiddenForCreateServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = post()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_UNAUTHORIZED)));
    verifyZeroInteractions(authService);
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForCreateServiceAccountEndpoint() {
    restAssured
        .given()
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(authService);
  }

  @Test
  public void shouldRetrieveServiceAccounts() {
    given(serviceAccountRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImplRepresentation<>(Lists.newArrayList(account)));

    get()
        .statusCode(HttpStatus.OK.value())
        .body("content.size()", is(1))
        .body("content[0].apiKey", is(account.getApiKey().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForGetServiceAccountsEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = get()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_UNAUTHORIZED)));
    verifyZeroInteractions(authService);
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForGetServiceAccountsEndpoint() {
    restAssured
        .given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteServiceAccount() {
    given(serviceAccountRepository.findOne(account.getApiKey()))
        .willReturn(account);

    delete().statusCode(HttpStatus.NO_CONTENT.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(authService).removeApiKey(account.getApiKey());
  }

  @Test
  public void shouldReturnForbiddenForDeleteServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = delete()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_UNAUTHORIZED)));
    verifyZeroInteractions(authService);
  }

  @Test
  public void shouldReturnNotFoundIfAccountNotExistForDeleteServiceAccountEndpoint() {
    given(serviceAccountRepository.findOne(account.getApiKey()))
        .willReturn(null);

    String response = delete()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_NOT_FOUND)));
    verifyZeroInteractions(authService);
  }

  @Test
  public void shouldReturnUnauthorizedWithoutAuthorizationForDeleteServiceAccountEndpoint() {
    restAssured
        .given()
        .pathParam(API_KEY, account.getApiKey())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(authService);
  }

  private ValidatableResponse post() {
    return startRequest(getTokenHeader())
        .when()
        .post(RESOURCE_URL)
        .then();
  }

  private ValidatableResponse get() {
    return startRequest(getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then();
  }

  private ValidatableResponse delete() {
    return startRequest(getTokenHeader())
        .pathParam(API_KEY, account.getApiKey())
        .when()
        .delete(ID_URL)
        .then();
  }
}
