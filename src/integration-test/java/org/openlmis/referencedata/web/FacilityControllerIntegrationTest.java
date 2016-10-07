package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

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
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.springframework.boot.test.mock.mockito.MockBean;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String SUPPLYING_URL = RESOURCE_URL + "/supplying";
  private static final String FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME =
      RESOURCE_URL + "/findFacilitiesWithSimilarCodeOrName";

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private SupplyLineService supplyLineService;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private SupervisoryNodeRepository supervisoryNodeRepository;

  private Integer currentInstanceNumber;
  private UUID programId;
  private UUID supervisoryNodeId;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
  }

  @Test
  public void shouldReturnSupplyingDepots() {
    int searchedFacilitiesAmt = 3;

    Program searchedProgram = generateProgram();
    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();

    List<SupplyLine> searchedSupplyLines = new ArrayList<>();
    for (int i = 0; i < searchedFacilitiesAmt; i++) {
      SupplyLine supplyLine = generateSupplyLine();
      supplyLine.setProgram(searchedProgram);
      supplyLine.setSupervisoryNode(searchedSupervisoryNode);

      searchedSupplyLines.add(supplyLine);
    }
    
    given(programRepository.findOne(programId)).willReturn(searchedProgram);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);
    given(supplyLineService.searchSupplyLines(searchedProgram, searchedSupervisoryNode))
        .willReturn(searchedSupplyLines);

    Facility[] response = restAssured.given()
        .queryParam("programId", programId)
        .queryParam("supervisoryNodeId", supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    assertEquals(searchedFacilitiesAmt, response.length);

    SupplyLine additionalSupplyLine = generateSupplyLine();
    Collection<Facility> searchedFacilities = searchedSupplyLines
        .stream().map(SupplyLine::getSupplyingFacility).collect(Collectors.toList());
    for (Facility facility : response) {
      assertTrue(searchedFacilities.contains(facility));
      assertNotEquals(facility, additionalSupplyLine.getSupplyingFacility());
    }
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    Facility generatedFacility = generateFacility();
    String similarCode = "Facility";
    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(generatedFacility);
    given(facilityRepository.findFacilitiesWithSimilarCodeOrName(similarCode,null))
        .willReturn(listToReturn);

    Facility[] response = restAssured.given()
        .queryParam("code", similarCode)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    List<Facility> facilities = Arrays.asList(response);
    assertEquals(1, facilities.size());
    assertEquals(generatedFacility.getCode(), facilities.get(0).getCode());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarName() {
    Facility generatedFacility = generateFacility();
    String similarName = "Facility";
    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(generatedFacility);
    given(facilityRepository.findFacilitiesWithSimilarCodeOrName(null,similarName))
        .willReturn(listToReturn);

    Facility[] response = restAssured.given()
        .queryParam("name", similarName)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    List<Facility> facilities = Arrays.asList(response);
    assertEquals(1, facilities.size());
    assertEquals(generatedFacility.getName(), facilities.get(0).getName());
  }

  @Test
  public void shouldNotFindFacilitiesWithIncorrectCodeAndName() {
    generateFacility();

    Facility[] response = restAssured.given()
        .queryParam("code", "IncorrectCode")
        .queryParam("name", "NotSimilarName")
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    List<Facility> facilities = Arrays.asList(response);
    assertEquals(0, facilities.size());
  }


  private SupplyLine generateSupplyLine() {
    SupplyLine supplyLine = new SupplyLine();
    supplyLine.setProgram(generateProgram());
    supplyLine.setSupervisoryNode(generateSupervisoryNode());
    supplyLine.setSupplyingFacility(generateFacility());
    return supplyLine;
  }

  private SupervisoryNode generateSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNodeId = UUID.randomUUID();
    supervisoryNode.setId(supervisoryNodeId);
    supervisoryNode.setCode("SupervisoryNode " + generateInstanceNumber());
    supervisoryNode.setFacility(generateFacility());
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new Program("Program " + generateInstanceNumber());
    programId = UUID.randomUUID();
    program.setId(programId);
    program.setPeriodsSkippable(false);
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
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel " + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone " + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType " + generateInstanceNumber());
    return facilityType;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
