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

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.ServiceAccount;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ServiceAccountDto;
import org.openlmis.referencedata.service.AuthService;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.testbuilder.ServiceAccountDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

public class ServiceAccountControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/serviceAccounts";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private AuthService authService;

  private ServiceAccount account;
  private ServiceAccountDto accountDto;

  private User user = new UserDataBuilder().build();

  private String apiKey = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    accountDto = new ServiceAccountDto();

    account = new ServiceAccountDataBuilder().build();
    account.export(accountDto);

    given(serviceAccountRepository.save(any(ServiceAccount.class)))
        .willAnswer(new SaveAnswer<>());

    given(authenticationHelper.getCurrentUser()).willReturn(user);
    given(authService.createApiKey()).willReturn(apiKey);

    mockUserHasRight(SERVICE_ACCOUNTS_MANAGE);
  }

  @Test
  public void shouldCreateServiceAccount() {
    ServiceAccountDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .as(ServiceAccountDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getLogin(), is(equalTo(apiKey)));
    assertThat(response.getCreatedBy(), is(equalTo(user.getId())));
    assertThat(response.getCreatedDate(), is(notNullValue()));

    verify(authenticationHelper).getCurrentUser();
    verify(authService).createApiKey();
  }

  @Test
  public void shouldReturnForbiddenForCreateServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .post(RESOURCE_URL)
        .then()
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

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("content.size()", is(1))
        .body("content[0].login", is(account.getLogin()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForGetServiceAccountsEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
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
    given(serviceAccountRepository.findOne(account.getId()))
        .willReturn(account);

    restAssured
        .given()
        .pathParam(ID, account.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.NO_CONTENT.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(authService).removeApiKey(account.getLogin());
  }

  @Test
  public void shouldReturnForbiddenForDeleteServiceAccountEndpointWhenUserHasNoRight() {
    mockUserHasNoRight(SERVICE_ACCOUNTS_MANAGE);

    String response = restAssured
        .given()
        .pathParam(ID, account.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value())
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, is(equalTo(ERROR_UNAUTHORIZED)));
    verifyZeroInteractions(authService);
  }

  @Test
  public void shouldReturnNotFoundIfAccountNotExistForDeleteServiceAccountEndpoint() {
    given(serviceAccountRepository.findOne(account.getId()))
        .willReturn(null);

    String response = restAssured
        .given()
        .pathParam(ID, account.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .delete(ID_URL)
        .then()
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
        .pathParam(ID, account.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(authService);
  }

}
