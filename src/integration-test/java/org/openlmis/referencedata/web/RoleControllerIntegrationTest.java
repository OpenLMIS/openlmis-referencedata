package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.dto.RoleDto;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class RoleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/roles";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ROLE_NAME = "role1";

  @MockBean
  private RoleRepository roleRepository;

  @MockBean
  private RightRepository rightRepository;

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
  public RoleControllerIntegrationTest() throws RightTypeException, RoleException {
    right1 = Right.newRight(RIGHT1_NAME, RightType.GENERAL_ADMIN);
    right2 = Right.newRight(RIGHT2_NAME, RightType.GENERAL_ADMIN);
    role = Role.newRole(ROLE_NAME, right1, right2);
    roleDto = new RoleDto();
    role.export(roleDto);
    roleId = UUID.randomUUID();
  }

  @Test
  public void shouldGetAllRoles() throws RightTypeException, RoleException {

    Set<Role> storedRoles = Sets.newHashSet(role,
        Role.newRole("role2", right1));
    given(roleRepository.findAll()).willReturn(storedRoles);

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

    given(roleRepository.findOne(roleId)).willReturn(role);

    RoleDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldPostRole() {

    given(roleRepository.findFirstByName(ROLE_NAME)).willReturn(null);
    given(rightRepository.findFirstByName(RIGHT1_NAME)).willReturn(right1);
    given(rightRepository.findFirstByName(RIGHT2_NAME)).willReturn(right2);

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

    assertEquals(roleDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutRole() {

    given(roleRepository.findOne(roleId)).willReturn(role);
    given(rightRepository.findFirstByName(RIGHT1_NAME)).willReturn(right1);
    given(rightRepository.findFirstByName(RIGHT2_NAME)).willReturn(right2);

    RoleDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldDeleteRole() {

    given(roleRepository.findOne(roleId)).willReturn(role);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", roleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
