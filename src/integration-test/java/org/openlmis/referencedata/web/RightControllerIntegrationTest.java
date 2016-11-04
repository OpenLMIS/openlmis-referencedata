package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
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
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RIGHT_NAME = "right";

  @MockBean
  private RightRepository rightRepository;

  private Right right;
  private RightDto rightDto;
  private UUID rightId;

  /**
   * Constructor for test class.
   */
  public RightControllerIntegrationTest() {
    right = Right.newRight(RIGHT_NAME, RightType.GENERAL_ADMIN);
    rightDto = new RightDto();
    right.export(rightDto);
    rightId = UUID.randomUUID();
  }

  @Test
  public void shouldGetAllRights() {

    Set<Right> storedRights = Sets.newHashSet(right,
        Right.newRight("right2", RightType.SUPERVISION));
    given(rightRepository.findAll()).willReturn(storedRights);

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

    given(rightRepository.findOne(rightId)).willReturn(right);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldPutRight() {

    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(null);

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
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteRight() {

    given(rightRepository.findOne(rightId)).willReturn(right);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", rightId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindRightByName() {

    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(right);

    RightDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("name", RIGHT_NAME)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(RightDto.class);

    assertEquals(RIGHT_NAME, response.getName());
    assertEquals(RightType.GENERAL_ADMIN, response.getType());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotFindRightByNameForNonExistingRight() {

    given(rightRepository.findFirstByName(RIGHT_NAME)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("name", RIGHT_NAME)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
