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
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.UUID;

@Ignore
public class RequisitionGroupControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/requisitionGroups";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");
  private static final String DESCRIPTION = "OpenLMIS";

  @Autowired
  private RequisitionGroupRepository repository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

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

  private RequisitionGroup requisitionGroup = new RequisitionGroup();

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

    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode("supervisoryNodeCode");
    supervisoryNode.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode);

    requisitionGroup.setCode("code");
    requisitionGroup.setName("name");
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    repository.save(requisitionGroup);
  }

  @Test
  public void shouldDeleteRequisitionGroup() {

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", requisitionGroup.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(repository.exists(requisitionGroup.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentRequisitionGroup() {

    repository.delete(requisitionGroup);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", requisitionGroup.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateRequisitionGroup() {

    repository.delete(requisitionGroup);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(requisitionGroup)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllRequisitionGroups() {

    RequisitionGroup[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(RequisitionGroup[].class);

    Iterable<RequisitionGroup> requisitionGroups = Arrays.asList(response);
    assertTrue(requisitionGroups.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenRequisitionGroup() {

    RequisitionGroup response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", requisitionGroup.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(RequisitionGroup.class);

    assertTrue(repository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentRequisitionGroup() {

    repository.delete(requisitionGroup);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", requisitionGroup.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateRequisitionGroup() {

    requisitionGroup.setDescription(DESCRIPTION);

    RequisitionGroup response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", requisitionGroup.getId())
          .body(requisitionGroup)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(RequisitionGroup.class);

    assertEquals(response.getDescription(), DESCRIPTION);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRequisitionGroupIfDoesNotExist() {

    repository.delete(requisitionGroup);
    requisitionGroup.setDescription(DESCRIPTION);

    RequisitionGroup response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", ID)
          .body(requisitionGroup)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(RequisitionGroup.class);

    assertEquals(response.getDescription(), DESCRIPTION);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
