package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.SupplyLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.UUID;

@Ignore
@SuppressWarnings("PMD.TooManyMethods")
public class SupplyLineControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/supplyLines";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");
  private static final String DESCRIPTION = "OpenLMIS";

  @Autowired
  private SupplyLineRepository supplyLineRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProgramRepository programRepository;

  private SupplyLine supplyLine;
  private Integer currentInstanceNumber;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
    supplyLine = generateSupplyLine();
  }

  @Test
  public void shouldFindSupplyLines() {
    SupplyLine[] response = restAssured.given()
        .queryParam("program", supplyLine.getProgram().getId())
        .queryParam("supervisoryNode", supplyLine.getSupervisoryNode().getId())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(SupplyLine[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for ( SupplyLine responseSupplyLine : response ) {
      assertEquals(
          supplyLine.getProgram().getId(),
          responseSupplyLine.getProgram().getId());
      assertEquals(
          supplyLine.getSupervisoryNode().getId(),
          responseSupplyLine.getSupervisoryNode().getId());
      assertEquals(
          supplyLine.getId(),
          responseSupplyLine.getId());
    }
  }

  @Test
  public void shouldDeleteSupplyLine() {

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supplyLine.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertFalse(supplyLineRepository.exists(supplyLine.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentSupplyLine() {

    supplyLineRepository.delete(supplyLine);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supplyLine.getId())
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSupplyLine() {

    supplyLineRepository.delete(supplyLine);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(supplyLine)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSupplyLine() {

    supplyLine.setDescription(DESCRIPTION);

    SupplyLine response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supplyLine.getId())
          .body(supplyLine)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupplyLine.class);

    assertEquals(response.getDescription(), DESCRIPTION);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewSupplyLineIfDoesNotExist() {

    supplyLineRepository.delete(supplyLine);
    supplyLine.setDescription(DESCRIPTION);

    SupplyLine response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", ID)
          .body(supplyLine)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupplyLine.class);

    assertEquals(response.getDescription(), DESCRIPTION);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSupplyLines() {

    SupplyLine[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(SupplyLine[].class);

    Iterable<SupplyLine> supplyLines = Arrays.asList(response);
    assertTrue(supplyLines.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenSupplyLine() {

    SupplyLine response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supplyLine.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(SupplyLine.class);

    assertTrue(supplyLineRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentSupplyLine() {

    supplyLineRepository.delete(supplyLine);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", supplyLine.getId())
          .when()
          .get(ID_URL)
          .then()
          .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private SupplyLine generateSupplyLine() {
    SupplyLine supplyLine = new SupplyLine();
    supplyLine.setProgram(generateProgram());
    supplyLine.setSupervisoryNode(generateSupervisoryNode());
    supplyLine.setSupplyingFacility(generateFacility());
    supplyLineRepository.save(supplyLine);
    return supplyLine;
  }

  private SupervisoryNode generateSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode("234");
    supervisoryNode.setFacility(generateFacility());
    supervisoryNodeRepository.save(supervisoryNode);
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new Program("890");
    program.setPeriodsSkippable(false);
    programRepository.save(program);
    return program;
  }

  private Facility generateFacility() {
    Integer instanceNumber = + generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility("FacilityCode" + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("FacilityName" + instanceNumber);
    facility.setDescription("FacilityDescription" + instanceNumber);
    facility.setActive(true);
    facilityRepository.save(facility);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel" + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone" + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType" + generateInstanceNumber());
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
