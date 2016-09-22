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
import org.openlmis.referencedata.dto.RightDto;
import org.openlmis.referencedata.repository.RightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;

public class RightControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/rights";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RIGHT_NAME = "right";
  private static final String DESCRIPTION = "OpenLMIS";

  @Autowired
  private RightRepository rightRepository;

  private Right right;
  private RightDto rightDto;

  /**
   * Constructor for test class.
   */
  public RightControllerIntegrationTest() {
    right = Right.newRight(RIGHT_NAME, RightType.GENERAL_ADMIN);
    rightDto = new RightDto();
    right.export(rightDto);
  }
  
  @Before
  public void setUp() {
    rightRepository.save(right);
    rightRepository.save(Right.newRight("right2", RightType.SUPERVISION));
  }

  @After
  public void cleanUp() {
    rightRepository.deleteAll();
  }

  @Test
  public void shouldGetAllRights() {

    RightDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto[].class);

    List<RightDto> rights = Arrays.asList(response);
    assertThat(rights.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetRight() {

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", right.getId())
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
  public void shouldNotGetNonExistingRight() {

    rightRepository.delete(right);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", right.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRight() {

    rightRepository.delete(right);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertTrue(rightRepository.exists(response.getId()));
    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RightType.GENERAL_ADMIN, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateExistingRight() {

    rightDto.setDescription(DESCRIPTION);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(rightDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RightType.GENERAL_ADMIN, response.getType());
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteRight() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", right.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertFalse(rightRepository.exists(right.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonExistingRight() {

    rightRepository.delete(right);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", right.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
