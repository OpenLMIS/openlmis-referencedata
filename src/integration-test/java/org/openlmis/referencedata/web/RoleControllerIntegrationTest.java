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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.dto.RoleDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class RoleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/roles";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ROLE_NAME = "role1";

  private Role role;
  private RoleDto roleDto;
  private UUID roleId;

  private Right right1;
  private Right right2;
  private static final String RIGHT1_NAME = "right1";
  private static final String RIGHT2_NAME = "right2";

  /**
   * Constructor for test class.
   */
  public RoleControllerIntegrationTest() {
    right1 = Right.newRight(RIGHT1_NAME, RightType.GENERAL_ADMIN);
    right2 = Right.newRight(RIGHT2_NAME, RightType.GENERAL_ADMIN);
    role = Role.newRole(ROLE_NAME, right1, right2);
    roleDto = new RoleDto();
    role.export(roleDto);
    roleId = UUID.randomUUID();
  }

  @Test
  public void shouldGetAllRoles() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);
    List<Role> storedRoles = Arrays.asList(role,
        Role.newRole("role2", right1));
    given(roleRepository.findAll()).willReturn(storedRoles);

    RoleDto[] response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RoleDto[].class);

    List<RoleDto> roles = Arrays.asList(response);
    assertThat(roles.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenGettingAllRolesWithoutRight() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USER_ROLES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);

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
  public void shouldGetRole() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);
    given(roleRepository.findOne(roleId)).willReturn(role);

    RoleDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", roleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RoleDto.class);

    assertEquals(roleDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenGettingRoleWithoutRight() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USER_ROLES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", roleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostRole() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    given(roleRepository.findFirstByName(ROLE_NAME)).willReturn(null);
    given(rightRepository.findFirstByName(RIGHT1_NAME)).willReturn(right1);
    given(rightRepository.findFirstByName(RIGHT2_NAME)).willReturn(right2);

    RoleDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(roleDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(RoleDto.class);

    assertEquals(roleDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenPostingRoleWithoutRight() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USER_ROLES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(roleDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutRole() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    given(roleRepository.findOne(roleId)).willReturn(role);
    given(rightRepository.findFirstByName(RIGHT1_NAME)).willReturn(right1);
    given(rightRepository.findFirstByName(RIGHT2_NAME)).willReturn(right2);

    RoleDto response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", roleId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(roleDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RoleDto.class);

    assertEquals(roleDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenUpdatingRoleWithoutRight() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USER_ROLES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", roleId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(roleDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteRole() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    given(roleRepository.findOne(roleId)).willReturn(role);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", roleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedWhenDeletingRoleWithoutRight() {
    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USER_ROLES_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT, false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", roleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnNotFoundIfEntityDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);
    given(roleRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);
    given(roleRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USER_ROLES_MANAGE_RIGHT);
    given(roleRepository.findOne(any(UUID.class))).willReturn(role);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
