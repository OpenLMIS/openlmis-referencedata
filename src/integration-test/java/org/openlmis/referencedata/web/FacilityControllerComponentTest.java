package org.openlmis.referencedata.web;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FacilityControllerComponentTest extends BaseWebComponentTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String SUPPLYING_URL = RESOURCE_URL + "/supplying";

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private SupplyLineRepository supplyLineRepository;

  @Autowired
  private ProgramRepository programRepository;

  private Integer currentInstanceNumber;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
  }

  @Test
  public void shouldReturnSupplyingDepots() {
    int searchedFacilitiesAmt = 3;

    Program searchedProgram = generateProgram();
    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();

    Collection<SupplyLine> searchedSupplyLines = new ArrayList<>();
    SupplyLine additionalSupplyLine = generateSupplyLine();

    for (int i = 0; i < searchedFacilitiesAmt; i++) {
      SupplyLine supplyLine = generateSupplyLine();
      supplyLine.setProgram(searchedProgram);
      supplyLine.setSupervisoryNode(searchedSupervisoryNode);
      supplyLineRepository.save(supplyLine);

      searchedSupplyLines.add(supplyLine);
    }

    Facility[] response = restAssured.given()
        .queryParam("program", searchedProgram.getId())
        .queryParam("supervisoryNode", searchedSupervisoryNode.getId())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(searchedFacilitiesAmt, response.length);

    Collection<Facility> searchedFacilities = searchedSupplyLines
        .stream().map(SupplyLine::getSupplyingFacility).collect(Collectors.toList());
    for (Facility facility : response) {
      assertTrue(searchedFacilities.contains(facility));
      assertNotEquals(facility, additionalSupplyLine.getSupplyingFacility());
    }
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
    supervisoryNode.setCode("SupervisoryNode " + generateInstanceNumber());
    supervisoryNode.setFacility(generateFacility());
    supervisoryNodeRepository.save(supervisoryNode);
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new Program("Program " + generateInstanceNumber());
    program.setPeriodsSkippable(false);
    programRepository.save(program);
    return program;
  }

  private Facility generateFacility() {
    Integer instanceNumber = generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility("FacilityCode " + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("FacilityName " + instanceNumber);
    facility.setDescription("FacilityDescription " + instanceNumber);
    facility.setEnabled(true);
    facility.setActive(true);
    facilityRepository.save(facility);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel " + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone " + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType " + generateInstanceNumber());
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
