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
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.UserPreference;
import org.openlmis.referencedata.repository.UserPreferenceRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class UserPreferenceControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/users/{userId}/preferences";
  private static final String QUANTITY_UNIT = "quantityUnit";
  private static final String PACKS = "PACKS";

  @MockBean
  private UserPreferenceRepository userPreferenceRepository;

  private final UUID userId = UUID.randomUUID();

  @Test
  public void getShouldReturnUserPreferences() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    given(userPreferenceRepository.findAllByUserId(userId))
        .willReturn(Collections.singletonList(new UserPreference(userId, QUANTITY_UNIT, PACKS)));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("userId", userId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body(QUANTITY_UNIT, is(PACKS));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnForbiddenForUnauthorizedToken() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("userId", userId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldSaveUserPreferences() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.existsById(userId)).willReturn(true);
    given(userPreferenceRepository.findByUserIdAndPreferenceKey(userId, QUANTITY_UNIT))
        .willReturn(Optional.empty());
    given(userPreferenceRepository.findAllByUserId(userId))
        .willReturn(Collections.singletonList(new UserPreference(userId, QUANTITY_UNIT, PACKS)));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("userId", userId)
        .body(Collections.singletonMap(QUANTITY_UNIT, PACKS))
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .body(QUANTITY_UNIT, is(PACKS));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnBadRequestForBlankPreferenceKey() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.existsById(userId)).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("userId", userId)
        .body(Collections.singletonMap("", PACKS))
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnNotFoundForMissingUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.existsById(userId)).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("userId", userId)
        .body(Collections.singletonMap(QUANTITY_UNIT, PACKS))
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
