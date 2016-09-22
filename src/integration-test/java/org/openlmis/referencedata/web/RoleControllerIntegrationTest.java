package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.dto.RoleDto;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;
import org.openlmis.referencedata.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RoleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/roles";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String ROLE_NAME = "role1";

  @Autowired
  private RoleRepository roleRepository;

  private Role role;
  private RoleDto roleDto;

  @Before
  public void setUp() throws RightTypeException, RoleException {
    role = Role.newRole(ROLE_NAME, Right.newRight("right1", RightType.GENERAL_ADMIN));
    roleRepository.save(role);
    roleRepository.save(Role.newRole("role2", Right.newRight("right2", RightType.SUPERVISION)));

    roleDto = new RoleDto();
    role.export(roleDto);
  }

  @After
  public void cleanUp() {
    roleRepository.deleteAll();
  }

  @Test
  public void shouldGetAllRoles() {

    RoleDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldGetRole() {

    RoleDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", role.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RoleDto.class);

    assertEquals(roleDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonExistingRole() {

    roleRepository.delete(role);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", role.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRoleOnPost() {

    roleRepository.delete(role);

    RoleDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(roleDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(RoleDto.class);

    assertTrue(roleRepository.exists(role.getId()));
    assertEquals(roleDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotCreateExistingRoleOnPost() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(role)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(409);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateExistingRoleOnPut() {

    roleDto.setDescription("Updated");

    RoleDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", role.getId())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(roleDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RoleDto.class);

    assertEquals(roleDto, response);
    Role storedRole = roleRepository.findOne(role.getId());
    assertEquals("Updated", storedRole.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRoleOnPut() throws RightTypeException, RoleException {

    UUID newRoleId = UUID.randomUUID();
    Role newRole = Role.newRole("role3", Right.newRight("right1", RightType.GENERAL_ADMIN));
    RoleDto newRoleDto = new RoleDto();
    newRole.export(newRoleDto);

    RoleDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", newRoleId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(newRoleDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RoleDto.class);

    assertEquals(newRoleDto, response);
    Role storedRole = roleRepository.findOne(newRoleId);
    storedRole.getRights().size();
    assertEquals(newRole, storedRole);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteRole() {

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", role.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertFalse(roleRepository.exists(role.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonExistingRole() {

    roleRepository.delete(role);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", role.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
