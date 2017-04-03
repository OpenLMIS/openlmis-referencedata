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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.repository.RightRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RightControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/rights";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String RIGHT_NAME = "right";
  private static final String ATTACHMENT_NAME = "attachment";

  @MockBean
  private RightRepository rightRepository;

  private Right right;
  private Right attachment;
  private RightDto rightDto;
  private UUID rightId;

  /**
   * Constructor for test class.
   */
  public RightControllerIntegrationTest() {
    right = Right.newRight(RIGHT_NAME, RightType.GENERAL_ADMIN);
    attachment = Right.newRight(ATTACHMENT_NAME, RightType.GENERAL_ADMIN);
    right.attach(attachment);
    rightDto = new RightDto();
    right.export(rightDto);
    rightId = UUID.randomUUID();
  }

  @Test
  public void getAllShouldGetAllRights() {
    mockUserHasRight(RightName.RIGHTS_VIEW);

    Set<Right> storedRights = Sets.newHashSet(right, attachment,
        Right.newRight("right2", RightType.SUPERVISION));
    given(rightRepository.findAll()).willReturn(storedRights);

    RightDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto[].class);

    List<RightDto> rights = Arrays.asList(response);
    assertThat(rights.size(), is(3));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAllShouldReturnForbiddenForUnauthorizedToken() {
    mockUserHasNoRight(RightName.RIGHTS_VIEW);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldGetRight() {

    given(rightRepository.findOne(rightId)).willReturn(right);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .pathParam("id", rightId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RightType.GENERAL_ADMIN, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnForbiddenForUnauthorizedToken() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", rightId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnNotFoundForNonExistingRight() {

    given(rightRepository.findOne(rightId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .pathParam("id", rightId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldCreateNewRightForNonExistingRight() {

    given(rightRepository.findFirstByName(ATTACHMENT_NAME)).willReturn(attachment);
    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(null);
    given(rightRepository.save(right)).willReturn(right);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RightType.GENERAL_ADMIN, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldUpdateRightForExistingRight() {

    given(rightRepository.findFirstByName(ATTACHMENT_NAME)).willReturn(attachment);
    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(right);
    given(rightRepository.save(right)).willReturn(right);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RightType.GENERAL_ADMIN, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnBadRequestForNonExistingAttachment() {

    given(rightRepository.findFirstByName(ATTACHMENT_NAME)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnForbiddenForUnauthorizedToken() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldDeleteRight() {

    given(rightRepository.findOne(rightId)).willReturn(right);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnForbiddenForUnauthorizedToken() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnNotFoundForNonExistingRight() {

    given(rightRepository.findOne(rightId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldFindRightByName() {
    mockUserHasRight(RightName.RIGHTS_VIEW);

    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(right);

    RightDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .queryParam("name", RIGHT_NAME)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto[].class);

    RightDto rightDto = response[0];
    assertEquals(RIGHT_NAME, rightDto.getName());
    assertEquals(RightType.GENERAL_ADMIN, rightDto.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnForbiddenForUnauthorizedToken() {
    mockUserHasNoRight(RightName.RIGHTS_VIEW);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("name", RIGHT_NAME)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnNotFoundForNonExistingRight() {
    mockUserHasRight(RightName.RIGHTS_VIEW);

    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getClientToken())
        .queryParam("name", RIGHT_NAME)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
