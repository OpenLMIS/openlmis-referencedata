package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.openlmis.referencedata.domain.RightName.REQUISITION_GROUPS_MANAGE;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.dto.RequisitionGroupBaseDto;
import org.openlmis.referencedata.dto.RequisitionGroupDto;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.validate.RequisitionGroupValidator;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
public class RequisitionGroupControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/requisitionGroups";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DESCRIPTION = "OpenLMIS";

  @MockBean
  private RequisitionGroupRepository requisitionGroupRepository;

  @MockBean
  private RequisitionGroupValidator requisitionGroupValidator;

  private RequisitionGroup requisitionGroup;
  private RequisitionGroupBaseDto requisitionGroupDto;
  private UUID requisitionGroupId;

  private SupervisoryNode supervisoryNode;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private Program program;
  private ProcessingSchedule processingSchedule;

  /**
   * Constructor for tests.
   */
  public RequisitionGroupControllerIntegrationTest() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("FO1");

    FacilityType facilityType = new FacilityType("FT1");

    GeographicLevel geoLevel = new GeographicLevel("GL1", 1);

    GeographicZone geoZone = new GeographicZone("GZ1", geoLevel);

    Facility facility = new Facility("F1");
    facility.setActive(true);
    facility.setGeographicZone(geoZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("SN1", facility);
    supervisoryNode.setId(UUID.randomUUID());

    program = new Program("PRO-1");
    processingSchedule = new ProcessingSchedule("SCH-1", "Monthly Schedule");

    requisitionGroup = new RequisitionGroup("RG1", "Requisition Group 1", supervisoryNode);
    supervisoryNode.setRequisitionGroup(requisitionGroup);

    requisitionGroupProgramSchedule = RequisitionGroupProgramSchedule
        .newRequisitionGroupProgramSchedule(requisitionGroup, program, processingSchedule, true);
    List<RequisitionGroupProgramSchedule> schedules = new ArrayList<>();
    schedules.add(requisitionGroupProgramSchedule);

    requisitionGroup.setRequisitionGroupProgramSchedules(schedules);

    requisitionGroupId = UUID.randomUUID();
    requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);
  }

  @Test
  public void shouldDeleteRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requisitionGroupDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPostRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requisitionGroupDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllRequisitionGroups() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    List<RequisitionGroup> storedRequisitionGroups = Arrays.asList(requisitionGroup,
        new RequisitionGroup("RG2", "Requisition Group 2", supervisoryNode));
    given(requisitionGroupRepository.findAll()).willReturn(storedRequisitionGroups);

    RequisitionGroupDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto[].class);

    assertEquals(storedRequisitionGroups.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetAllRequisitionGroupsIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutRequisitionGroup() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);
    given(requisitionGroupRepository.save(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    RequisitionGroupDto requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .body(requisitionGroupDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutRequisitionGroupIfUserHasNoRight() {
    mockUserHasNoRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(requisitionGroup);
    given(requisitionGroupRepository.save(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    RequisitionGroupDto requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .body(requisitionGroupDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(Matchers.equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRequisitionGroupIfDoesNotExist() {
    mockUserHasRight(REQUISITION_GROUPS_MANAGE);

    requisitionGroup.setDescription(DESCRIPTION);
    given(requisitionGroupRepository.findOne(requisitionGroupId)).willReturn(null);
    given(requisitionGroupRepository.save(any(RequisitionGroup.class)))
        .willReturn(requisitionGroup);

    RequisitionGroupDto requisitionGroupDto = new RequisitionGroupDto();
    requisitionGroup.export(requisitionGroupDto);

    RequisitionGroupDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupId)
        .body(requisitionGroupDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupDto.class);

    assertEquals(requisitionGroupDto, response);
    assertEquals(DESCRIPTION, response.getDescription());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
