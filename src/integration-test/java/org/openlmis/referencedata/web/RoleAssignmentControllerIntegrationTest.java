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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.UserRoleAssignmentDto;
import org.springframework.http.HttpHeaders;

public class RoleAssignmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/roleAssignments";

  @Test
  public void shouldGetAllRoleAssignments() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();
    UUID programId = UUID.randomUUID();
    given(roleAssignmentRepository.findAllWithUser()).willReturn(Collections.singletonList(
        new UserRoleAssignmentDto(userId, roleId, programId, null, null)));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body("content.size()", is(1))
        .body("content[0].userId", is(userId.toString()))
        .body("content[0].roleId", is(roleId.toString()))
        .body("content[0].programId", is(programId.toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetAllRoleAssignmentsIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
