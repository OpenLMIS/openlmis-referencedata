package org.openlmis.referencedata.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.javers.common.collections.Sets;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.openlmis.referencedata.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.TooManyMethods"})
public class SupervisoryNodeControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/supervisoryNodes";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SUPERVISING_USERS_URL = ID_URL + "/supervisingUsers";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String RIGHT_ID_PARAM = "rightId";
  private static final String PROGRAM_ID_PARAM = "programId";

  @MockBean
  private SupervisoryNodeRepository repository;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private FacilityRepository facilityRepository;
  
  @MockBean
  private RightRepository rightRepository;
  
  @MockBean
  private UserRepository userRepository;

  @MockBean
  private RequisitionGroupProgramScheduleService requisitionGroupProgramScheduleService;

  private SupervisoryNode supervisoryNode;
  private SupervisoryNodeDto supervisoryNodeDto;
  private UUID supervisoryNodeId;
  private Facility facility;
  private Program program;
  private Right right;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private RequisitionGroup requisitionGroup;
  private UUID facilityId;
  private UUID programId;
  private UUID rightId;

  /**
   * Constructor for tests.
   */
  public SupervisoryNodeControllerIntegrationTest() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperator.setCode("facilityOperator");

    final FacilityType facilityType = new FacilityType("facilityTypeCode");

    final GeographicLevel geoLevel = new GeographicLevel("geoCode", 1);

    final GeographicZone geoZone = new GeographicZone("geoZoneCode", geoLevel);

    facility = new Facility("facilityCode");
    facility.setActive(true);
    facility.setGeographicZone(geoZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);
    facility.setEnabled(true);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("supervisoryNodeCode", facility);
    supervisoryNodeDto = new SupervisoryNodeDto();
    supervisoryNode.export(supervisoryNodeDto);
    supervisoryNodeId = UUID.randomUUID();

    program = new Program("PRO-1");
    requisitionGroup = new RequisitionGroup();
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    requisitionGroupProgramSchedule.setRequisitionGroup(requisitionGroup);

    right = Right.newRight("right1", RightType.SUPERVISION);
    
    facilityId = UUID.randomUUID();
    programId = UUID.randomUUID();
    rightId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);
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
  public void shouldRejectDeleteSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

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
  public void shouldRejectPostSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(supervisoryNodeDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

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
  public void shouldRejectPutSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .body(supervisoryNodeDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllSupervisoryNodes() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

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
  public void shouldRejectGetAllSupervisoryNodesIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

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

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

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
  public void shouldRejectGetSupervisoryNodeIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);

    String messageKey = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldGetSupervisingUsers() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    Role role = Role.newRole("role", right);
    role.setId(UUID.randomUUID());

    User supervisingUser = new UserBuilder("supervisingUser", "Supervising", "User",
        "a@b.com").createUser();
    supervisingUser.assignRoles(
        new SupervisionRoleAssignment(role, supervisingUser, program, supervisoryNode));

    Set<User> supervisingUsers = Sets.asSet(supervisingUser);

    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);
    given(rightRepository.findOne(rightId)).willReturn(right);
    given(programRepository.findOne(programId)).willReturn(program);
    given(userRepository.findSupervisingUsersBy(right, supervisoryNode, program))
        .willReturn(supervisingUsers);

    UserDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID_PARAM, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    assertThat(response.length, is(1));
    assertEquals(supervisingUser.getUsername(), response[0].getUsername());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnBadRequestIfRightNotFound() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);
    given(rightRepository.findOne(rightId)).willReturn(null);
    given(programRepository.findOne(programId)).willReturn(program);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID_PARAM, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnBadRequestIfProgramNotFound() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(repository.findOne(supervisoryNodeId)).willReturn(supervisoryNode);
    given(rightRepository.findOne(rightId)).willReturn(right);
    given(programRepository.findOne(programId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID_PARAM, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnForbiddenForUnauthorizedToken() {

    doThrow(new UnauthorizedException(
        new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, RightName.USERS_MANAGE_RIGHT)))
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID_PARAM, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void findSupervisingUsersShouldReturnNotFoundIfSupervisoryNodeNotFound() {

    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(repository.findOne(supervisoryNodeId)).willReturn(null);
    given(rightRepository.findOne(rightId)).willReturn(right);
    given(programRepository.findOne(programId)).willReturn(program);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", supervisoryNodeId)
        .queryParam(RIGHT_ID_PARAM, rightId)
        .queryParam(PROGRAM_ID_PARAM, programId)
        .when()
        .get(SUPERVISING_USERS_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProcessingScheduleByFacilityAndProgram() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(requisitionGroupProgramSchedule);

    SupervisoryNode[] response = searchForSupervisoryNode(programId, facilityId, 200)
        .extract().as(SupervisoryNode[].class);

    assertEquals(supervisoryNode, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetProcessingScheduleByFacilityAndPrograIfUserHasNoRight() {
    mockUserHasNoRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(requisitionGroupProgramSchedule);

    String messageKey = searchForSupervisoryNode(programId, facilityId, 403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenSearchingForNonExistingSupervisoryNode() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(program);
    given(requisitionGroupProgramScheduleService.searchRequisitionGroupProgramSchedule(
        program, facility)).willReturn(null);

    searchForSupervisoryNode(programId, facilityId, 404);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupervisoryNodeWithBadParameters() {
    mockUserHasRight(RightName.SUPERVISORY_NODES_MANAGE);

    given(facilityRepository.findOne(facilityId)).willReturn(facility);
    given(programRepository.findOne(programId)).willReturn(null);

    searchForSupervisoryNode(programId, facilityId, 400);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ValidatableResponse searchForSupervisoryNode(UUID programId, UUID facilityId,
                                                       int expectedCode) {
    return restAssured
      .given()
      .queryParam(ACCESS_TOKEN, getToken())
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .queryParam("facilityId", facilityId)
      .queryParam(PROGRAM_ID_PARAM, programId)
      .when()
      .get(SEARCH_URL)
      .then()
      .statusCode(expectedCode);
  }
}
