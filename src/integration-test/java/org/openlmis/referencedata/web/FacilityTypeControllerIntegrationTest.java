package org.openlmis.referencedata.web;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FacilityTypeControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/facilityTypes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String DESCRIPTION = "OpenLMIS";

  @MockBean
  private FacilityTypeRepository facilityTypeRepository;

  private FacilityType facilityType;
  private UUID facilityTypeId;

  public FacilityTypeControllerIntegrationTest() {
    facilityType = new FacilityType("code");
    facilityTypeId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteFacilityType() {

    given(facilityTypeRepository.findOne(facilityTypeId)).willReturn(facilityType);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostFacilityType() {

    FacilityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityType)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(FacilityType.class);

    assertEquals(facilityType, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutFacilityType() {

    facilityType.setDescription(DESCRIPTION);

    FacilityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeId)
        .body(facilityType)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityType.class);

    assertEquals(facilityType, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllFacilityTypes() {

    List<FacilityType> storedFacilityTypes = Arrays.asList(facilityType, new FacilityType("code2"));
    given(facilityTypeRepository.findAll()).willReturn(storedFacilityTypes);

    FacilityType[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityType[].class);

    assertEquals(storedFacilityTypes.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetFacilityType() {

    given(facilityTypeRepository.findOne(facilityTypeId)).willReturn(facilityType);

    FacilityType response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityTypeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityType.class);

    assertEquals(facilityType, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
