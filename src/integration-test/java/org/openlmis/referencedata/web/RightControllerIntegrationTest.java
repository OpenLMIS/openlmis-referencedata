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
import org.openlmis.referencedata.util.messagekeys.RightMessageKeys;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class RightControllerIntegrationTest extends AuditLogWebIntegrationTest<Right> {

  private static final String RESOURCE_URL = "/api/rights";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_URL = ID_URL + "/auditLog";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String RIGHT_NAME = "right";
  private static final String ATTACHMENT_NAME = "attachment";
  private static final RightType RIGHT_TYPE = RightType.GENERAL_ADMIN;

  private Right right;
  private Right attachment;
  private RightDto rightDto;
  private UUID rightId;

  /**
   * Constructor for test class.
   */
  public RightControllerIntegrationTest() {
    right = Right.newRight(RIGHT_NAME, RIGHT_TYPE);
    attachment = Right.newRight(ATTACHMENT_NAME, RIGHT_TYPE);
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
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldGetRight() {
    mockClientHasRootAccess();

    given(rightRepository.findOne(rightId)).willReturn(right);

    RightDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .pathParam("id", rightId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RIGHT_TYPE, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnForbiddenForUnauthorizedToken() {
    mockClientHasNoRootAccess();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", rightId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getShouldReturnNotFoundForNonExistingRight() {
    mockClientHasRootAccess();

    given(rightRepository.findOne(rightId)).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .pathParam("id", rightId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldCreateNewRightForNonExistingRight() {
    mockClientHasRootAccess();

    given(rightRepository.findFirstByName(ATTACHMENT_NAME)).willReturn(attachment);
    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(null);
    given(rightRepository.save(right)).willReturn(right);

    RightDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RIGHT_TYPE, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldUpdateRightForExistingRight() {
    mockClientHasRootAccess();

    given(rightRepository.findFirstByName(ATTACHMENT_NAME)).willReturn(attachment);
    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(right);
    given(rightRepository.save(right)).willReturn(right);

    RightDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RIGHT_TYPE, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void putShouldReturnBadRequestForNonExistingAttachment() {
    mockClientHasRootAccess();

    given(rightRepository.findFirstByName(ATTACHMENT_NAME)).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
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
    mockClientHasNoRootAccess();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    mockClientHasRootAccess();

    given(rightRepository.findOne(rightId)).willReturn(right);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnForbiddenForUnauthorizedToken() {
    mockClientHasNoRootAccess();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void deleteShouldReturnNotFoundForNonExistingRight() {
    mockClientHasRootAccess();

    given(rightRepository.findOne(rightId)).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldFindRightByNameAndType() {
    mockUserHasRight(RightName.RIGHTS_VIEW);

    given(rightRepository.searchRights(RIGHT_NAME, RIGHT_TYPE)).willReturn(
            Collections.singleton(right));

    RightDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
        .queryParam("name", RIGHT_NAME)
        .queryParam("type", RIGHT_TYPE.toString())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto[].class);

    RightDto rightDto = response[0];
    assertEquals(RIGHT_NAME, rightDto.getName());
    assertEquals(RIGHT_TYPE, rightDto.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnForbiddenForUnauthorizedToken() {
    mockUserHasNoRight(RightName.RIGHTS_VIEW);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("name", RIGHT_NAME)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void searchShouldReturnBadRequestForInvalidType() {
    mockUserHasRight(RightName.RIGHTS_VIEW);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getClientTokenHeader())
            .queryParam("type", "INVALID_TYPE")
            .when()
            .get(SEARCH_URL)
            .then()
            .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Override
  protected void mockHasNoAuditRight() {
    mockClientHasNoRootAccess();
  }

  @Override
  protected void mockHasAuditRight() {
    mockClientHasRootAccess();
  }

  @Override
  protected Right getInstance() {
    return right;
  }

  @Override
  protected CrudRepository<Right, UUID> getRepository() {
    return rightRepository;
  }

  @Override
  protected String getAuditAddress() {
    return AUDIT_URL;
  }

  @Override
  protected String getErrorNotFoundMessage() {
    return RightMessageKeys.ERROR_NOT_FOUND;
  }

}
