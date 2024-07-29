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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.IntegrationEmail;
import org.openlmis.referencedata.dto.IntegrationEmailDto;
import org.openlmis.referencedata.testbuilder.IntegrationEmailDataBuilder;
import org.openlmis.referencedata.util.messagekeys.IntegrationEmailMessageKeys;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class IntegrationEmailControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = IntegrationEmailController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";

  private static final String EMAIL = "email";

  private IntegrationEmail integrationEmail = new IntegrationEmailDataBuilder().build();
  private IntegrationEmailDto integrationEmailDto =
      IntegrationEmailDto.newInstance(integrationEmail);

  private GlobalId globalId = new UnboundedValueObjectId(IntegrationEmail.class.getSimpleName());
  private ValueChange change = new ValueChange(globalId, EMAIL, "email1", "email2");

  private CommitId commitId = new CommitId(1, 0);
  private CommitMetadata commitMetadata = new CommitMetadata(
      "admin", Maps.newHashMap(), LocalDateTime.now(), commitId);

  @Before
  public void setUp() {
    given(integrationEmailRepository.saveAndFlush(any(IntegrationEmail.class)))
        .willAnswer(new SaveAnswer<>());
    change.bindToCommit(commitMetadata);
  }

  @Test
  public void shouldReturnPageOfIntegrationEmails() {
    given(integrationEmailRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(integrationEmail)));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1))
        .body("content[0].id", is(integrationEmail.getId().toString()))
        .body("content[0].email", is(integrationEmail.getEmail()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAllIntegrationEmailsEndpointIfUserIsNotAuthorized() {
    restAssured.given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateIntegrationEmail() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(integrationEmailDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, is(notNullValue()))
        .body(EMAIL, is(integrationEmailDto.getEmail()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateIntegrationEmailEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(integrationEmailDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenIntegrationEmail() {
    given(integrationEmailRepository.findById(integrationEmailDto.getId()))
        .willReturn(Optional.of(integrationEmail));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(integrationEmailDto.getId().toString()))
        .body(EMAIL, is(integrationEmailDto.getEmail()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfIntegrationEmailDoesNotExistForGivenEndpoint() {
    given(integrationEmailRepository.findById(integrationEmailDto.getId()))
        .willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetIntegrationEmailEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateIntegrationEmail() {
    given(integrationEmailRepository.findById(integrationEmailDto.getId()))
        .willReturn(Optional.of(integrationEmail));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationEmailDto.getId().toString())
        .body(integrationEmailDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(integrationEmailDto.getId().toString()))
        .body(EMAIL, is(integrationEmailDto.getEmail()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateIntegrationEmailIfIntegrationEmailDoesNotExistForUpdateEndpoint()
      throws JsonProcessingException {
    String integrationEmailJson = "{\"email\":\"" + integrationEmailDto.getEmail() + "\"}";
    Map<String, String> integrationEmailMap = new ObjectMapper().readValue(integrationEmailJson,
        new TypeReference<Map<String, String>>() {});
    UUID pathId = UUID.randomUUID();
    given(integrationEmailRepository.findById(pathId)).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, pathId)
        .body(integrationEmailMap)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(pathId.toString()))
        .body(EMAIL, is(integrationEmailDto.getEmail()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestMessageIfIntegrationEmailCannotBeUpdated() {
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, UUID.randomUUID().toString())
        .body(integrationEmailDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateIntegrationEmailEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, integrationEmailDto.getId().toString())
        .body(integrationEmailDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteIntegrationEmail() {
    given(integrationEmailRepository.existsById(integrationEmailDto.getId())).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfIntegrationEmailDoesNotExistForDeleteEndpoint() {
    given(integrationEmailRepository.existsById(integrationEmailDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteIntegrationEmailEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfIntegrationEmailDoesNotExistForAuditLogEndpoint() {
    given(integrationEmailRepository.existsById(integrationEmailDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAuditLogEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, integrationEmailDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}
