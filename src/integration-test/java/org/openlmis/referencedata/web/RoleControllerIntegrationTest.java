package org.openlmis.referencedata.web;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;

@Ignore
public class RoleControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/roles";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";

  @Autowired
  private RoleRepository roleRepository;

  private Role role = new Role("roleName", Right.newRight("rightName", RightType.GENERAL_ADMIN));

  public RoleControllerIntegrationTest() throws RightTypeException {
  }

  @Before
  public void setUp() {
    roleRepository.save(role);
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

    Assert.assertFalse(roleRepository.exists(role.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllRoles() {

    Role[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(Role[].class);

    Iterable<Role> roles = Arrays.asList(response);
    assertTrue(roles.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenRole() {

    Role response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", role.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(Role.class);

    assertTrue(roleRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
