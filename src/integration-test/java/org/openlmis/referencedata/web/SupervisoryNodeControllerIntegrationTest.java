package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SupervisoryNodeControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/supervisoryNodes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  @MockBean
  private SupervisoryNodeRepository repository;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

  private SupervisoryNode supervisoryNode;
  private SupervisoryNodeDto supervisoryNodeDto;
  private UUID supervisoryNodeId;
  private Facility facility;
  private Program program;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private RequisitionGroup requisitionGroup;
  private UUID facilityId;
  private UUID programId;

  /**
   * Constructor for tests.
   */
  public SupervisoryNodeControllerIntegrationTest() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("facilityOperator");

    final FacilityType facilityType = new FacilityType("facilityTypeCode");

    final GeographicLevel geoLevel = new GeographicLevel("geoCode", 1);

    final GeographicZone geoZone = new GeographicZone("geoZoneCode", geoLevel);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("supervisoryNodeCode", facility);
    supervisoryNodeDto = new SupervisoryNodeDto();
    supervisoryNode.export(supervisoryNodeDto);
    supervisoryNodeId = UUID.randomUUID();

    facility = new Facility("facilityCode");
    facility.setActive(true);
    facility.setGeographicZone(geoZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);

    program = new Program("PRO-1");
    requisitionGroup = new RequisitionGroup();
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    requisitionGroupProgramSchedule.setRequisitionGroup(requisitionGroup);
    facilityId = UUID.randomUUID();
    programId = UUID.randomUUID();

  }

  @Test
  public void shouldDeleteSupervisoryNode() {

    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostSupervisoryNode() {

    SupervisoryNodeDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(SupervisoryNodeDto.class);

    assertEquals(supervisoryNodeDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutSupervisoryNode() {

    supervisoryNode.setDescription("OpenLMIS");
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    SupervisoryNodeDto supervisoryNodeDto = new SupervisoryNodeDto();
    supervisoryNode.export(supervisoryNodeDto);

    SupervisoryNodeDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(SupervisoryNodeDto.class);

    assertEquals(supervisoryNodeDto, response);
    assertEquals("OpenLMIS", response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSupervisoryNodes() {

    List<SupervisoryNode> storedSupervisoryNodes = Arrays.asList(supervisoryNode,
        SupervisoryNode.newSupervisoryNode("SN2", new Facility("F2")));
    given(repository.findAll()).willReturn(storedSupervisoryNodes);

    SupervisoryNodeDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(SupervisoryNodeDto[].class);

    assertEquals(storedSupervisoryNodes.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupervisoryNode() {

    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    SupervisoryNodeDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(SupervisoryNodeDto.class);

    assertEquals(supervisoryNodeDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingScheduleByFacilityAndProgram() {

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(requisitionGroupProgramSchedule);

    SupervisoryNode[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", facilityId)
        .queryParam("programId", programId)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(SupervisoryNode[].class);

    assertEquals(supervisoryNode, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
