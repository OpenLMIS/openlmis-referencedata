package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.Money;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.springframework.boot.test.mock.mockito.MockBean;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"PMD.TooManyMethods"})
public class FacilityControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String PROGRAM_ID = "programId";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String SUPPLYING_URL = RESOURCE_URL + "/supplying";
  private static final String FIND_FACILITIES_WITH_SIMILAR_CODE_OR_NAME =
      RESOURCE_URL + "/search";

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private SupplyLineService supplyLineService;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private FacilityTypeApprovedProductRepository facilityTypeApprovedProductRepository;

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
        .queryParam(PROGRAM_ID, programId)
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
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingSupervisorNode() {
    Program searchedProgram = generateProgram();
    supervisoryNodeId = UUID.randomUUID();

    given(programRepository.findOne(programId)).willReturn(searchedProgram);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(null);


    restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam("supervisoryNodeId", supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingProgram() {
    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();
    programId = UUID.randomUUID();

    given(programRepository.findOne(programId)).willReturn(null);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);

    restAssured.given()
        .queryParam(PROGRAM_ID, programId)
        .queryParam("supervisoryNodeId", supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCode() {
    Facility generatedFacility = generateFacility();
    String similarCode = "Facility";
    List<Facility> listToReturn = new ArrayList<>();
    listToReturn.add(generatedFacility);
    given(facilityRepository.findFacilitiesByCodeOrName(similarCode,null))
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
    given(facilityRepository.findFacilitiesByCodeOrName(null,similarName))
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

  @Test
  public void shouldFindApprovedProductsForFacility() {
    when(facilityRepository.findOne(any(UUID.class))).thenReturn(generateFacility());
    when(facilityTypeApprovedProductRepository.searchProducts(any(UUID.class), any(UUID.class),
        eq(false))).thenReturn(generateFacilityTypeApprovedProducts());

    List<Map<String, ?>> productDtos = restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(200)
        .extract().as(List.class);

    assertEquals(1, productDtos.size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestWhenLookingForProductsInNonExistantFacility() {
    when(facilityRepository.findOne(any(UUID.class))).thenReturn(null);

    restAssured.given()
        .queryParam(PROGRAM_ID, UUID.randomUUID())
        .queryParam("fullSupply", false)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL + "/" + UUID.randomUUID() + "/approvedProducts")
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
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

  private List<FacilityTypeApprovedProduct> generateFacilityTypeApprovedProducts() {
    ProductCategory category = ProductCategory.createNew(Code.code("gloves"));
    category.setId(UUID.randomUUID());
    OrderableProduct orderableProduct = GlobalProduct.newGlobalProduct(
        "gloves", "pair", "Gloves", "testDesc", 6, 3, false);
    orderableProduct.setId(UUID.randomUUID());
    ProgramProduct programProduct = ProgramProduct.createNew(generateProgram(), category,
        orderableProduct, 0, true, false, 0, 0, new Money("0"));
    programProduct.setId(UUID.randomUUID());
    FacilityTypeApprovedProduct ftap = new FacilityTypeApprovedProduct();
    ftap.setProgramProduct(programProduct);
    ftap.setId(UUID.randomUUID());
    ftap.setMinMonthsOfStock(1d);
    ftap.setMaxMonthsOfStock(3d);
    ftap.setFacilityType(generateFacilityType());
    ftap.setEmergencyOrderPoint(1d);
    List<FacilityTypeApprovedProduct> products = new ArrayList<>();
    products.add(ftap);
    return products;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
