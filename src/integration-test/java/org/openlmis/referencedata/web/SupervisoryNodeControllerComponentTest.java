package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;

@Ignore
public class SupervisoryNodeControllerComponentTest extends BaseWebComponentTest {

  private static final String RESOURCE_URL = "/api/supervisoryNodes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";

  @Autowired
  private SupervisoryNodeRepository repository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityOperatorRepository facilityOperatorRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  private SupervisoryNode supervisoryNode;

  @Before
  public void setUp() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("facilityOperator");
    facilityOperatorRepository.save(facilityOperator);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode("facilityTypeCode");
    facilityTypeRepository.save(facilityType);

    GeographicLevel geoLevel = new GeographicLevel();
    geoLevel.setCode("geoCode");
    geoLevel.setLevelNumber(1);
    geographicLevelRepository.save(geoLevel);

    GeographicZone geoZone = new GeographicZone();
    geoZone.setCode("geoZoneCode");
    geoZone.setLevel(geoLevel);
    geographicZoneRepository.save(geoZone);

    Facility facility = new Facility("facilityCode");
    facility.setActive(true);
    facility.setGeographicZone(geoZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);
    facilityRepository.save(facility);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("supervisoryNodeCode", facility);
    repository.save(supervisoryNode);
  }

  @Test
  public void shouldDeleteSupervisoryNode() {

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(repository.exists(supervisoryNode.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSupervisoryNode() {

    repository.delete(supervisoryNode);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(supervisoryNode)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSupervisoryNode() {

    supervisoryNode.setCode("OpenLMIS");

    SupervisoryNode response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .body(supervisoryNode)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode.class);

    assertEquals(response.getCode(), "OpenLMIS");
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSupervisoryNodes() {

    SupervisoryNode[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode[].class);

    Iterable<SupervisoryNode> supervisoryNodes = Arrays.asList(response);
    assertTrue(supervisoryNodes.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenSupervisoryNode() {

    SupervisoryNode response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supervisoryNode.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupervisoryNode.class);

    assertTrue(repository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
