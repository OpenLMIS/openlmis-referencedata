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
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.openlmis.referencedata.domain.BaseEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpHeaders;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

public abstract class AuditLogWebIntegrationTest<T extends BaseEntity>
    extends BaseWebIntegrationTest {

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    UUID instanceId = UUID.randomUUID();

    mockHasAuditRight();
    given(getRepository().findOne(instanceId)).willReturn(null);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", instanceId)
        .when()
        .get(getAuditAddress())
        .then()
        .statusCode(404)
        .extract()
        .path(MESSAGE_KEY);

    verify(getRepository()).findOne(instanceId);

    assertThat(messageKey, is(equalTo(getErrorNotFoundMessage())));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    UUID instanceId = UUID.randomUUID();

    mockHasNoAuditRight();
    given(getRepository().findOne(instanceId)).willReturn(null);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", instanceId)
        .when()
        .get(getAuditAddress())
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    verify(getRepository(), never()).findOne(instanceId);

    assertThat(
        messageKey,
        isOneOf(MESSAGEKEY_ERROR_UNAUTHORIZED, MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC)
    );
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    mockHasAuditRight();
    given(getRepository().findOne(any(UUID.class))).willReturn(getInstance());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(getAuditAddress())
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  protected abstract void mockHasNoAuditRight();

  protected abstract void mockHasAuditRight();

  protected abstract T getInstance();

  protected abstract CrudRepository<T, UUID> getRepository();

  protected abstract String getAuditAddress();

  protected abstract String getErrorNotFoundMessage();

}
