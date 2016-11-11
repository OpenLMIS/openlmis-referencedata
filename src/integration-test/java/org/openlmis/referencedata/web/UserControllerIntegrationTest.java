package org.openlmis.referencedata.web;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Sets;

import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleAssignmentException;
import org.openlmis.referencedata.exception.RoleException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.util.AuthUserRequest;
import org.openlmis.referencedata.util.PasswordChangeRequest;
import org.openlmis.referencedata.util.PasswordResetRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class UserControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/users";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String HAS_RIGHT_URL = ID_URL + "/hasRight";
  private static final String PROGRAMS_URL = ID_URL + "/programs";
  private static final String SUPERVISED_FACILITIES_URL = ID_URL + "/supervisedFacilities";
  private static final String RESET_PASSWORD_URL = RESOURCE_URL + "/passwordReset";
  private static final String CHANGE_PASSWORD_URL = RESOURCE_URL + "/changePassword";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String USERNAME = "username";
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";
  private static final String HOME_FACILITY = "homeFacility";
  private static final String ACTIVE = "active";
  private static final String VERIFIED = "verified";
  private static final String SUPERVISION_RIGHT_NAME = "supervisionRight";
  private static final String PROGRAM1_CODE = "P1";
  private static final String PROGRAM2_CODE = "P2";
  private static final String SUPERVISORY_NODE_CODE = "SN1";
  private static final String WAREHOUSE_CODE = "W1";
  private static final String HOME_FACILITY_CODE = "HF1";
  private static final String USER_API_STRING = "/auth/api/users";
  private static final String RIGHT_ID_STRING = "rightId";
  private static final String PROGRAM_ID_STRING = "programId";

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private UserService userService;

  @MockBean
  private GeographicLevelRepository geographicLevelRepository;

  @MockBean
  private GeographicZoneRepository geographicZoneRepository;

  @MockBean
  private FacilityTypeRepository facilityTypeRepository;

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private RoleRepository roleRepository;

  @MockBean
  private RightRepository rightRepository;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @MockBean
  private RequisitionGroupRepository requisitionGroupRepository;

  private User user1;
  private UUID userId;
  private Facility homeFacility;
  private UUID homeFacilityId;

  private static Integer currentInstanceNumber = 0;
  private Role adminRole;
  private UUID adminRoleId;
  private Right supervisionRight;
  private UUID supervisionRightId;
  private Role supervisionRole;
  private UUID supervisionRoleId;
  private Program program1;
  private UUID program1Id;
  private Program program2;
  private UUID program2Id;
  private SupervisoryNode supervisoryNode;
  private Role fulfillmentRole;
  private UUID fulfillmentRoleId;
  private Facility warehouse;

  /**
   * Constructor for test class.
   */
  public UserControllerIntegrationTest() throws RoleException, RoleAssignmentException,
      RightTypeException {
    user1 = generateUser();
    assignUserRoles(user1);
    userId = UUID.randomUUID();
  }

  @Test
  public void shouldGetAllUsers() {

    Set<User> storedUsers = Sets.newHashSet(user1, generateUser());
    given(userRepository.findAll()).willReturn(storedUsers);

    UserDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    List<UserDto> users = Arrays.asList(response);
    assertThat(users.size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUser() {

    UserDto userDto = new UserDto();
    user1.export(userDto);
    given(userRepository.findOne(userId)).willReturn(user1);

    UserDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", userId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertEquals(userDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutUser() {

    UserDto userDto = new UserDto();
    user1.export(userDto);
    given(facilityRepository.findFirstByCode(userDto.fetchHomeFacilityCode()))
        .willReturn(homeFacility);
    given(roleRepository.findOne(adminRoleId)).willReturn(adminRole);
    given(roleRepository.findOne(supervisionRoleId)).willReturn(supervisionRole);
    given(programRepository.findByCode(Code.code(PROGRAM1_CODE))).willReturn(program1);
    given(programRepository.findByCode(Code.code(PROGRAM2_CODE))).willReturn(program2);
    given(supervisoryNodeRepository.findByCode(SUPERVISORY_NODE_CODE)).willReturn(supervisoryNode);
    given(roleRepository.findOne(fulfillmentRoleId)).willReturn(fulfillmentRole);
    given(facilityRepository.findFirstByCode(WAREHOUSE_CODE)).willReturn(warehouse);

    UserDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(userDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertEquals(userDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteUser() {

    given(userRepository.findOne(userId)).willReturn(user1);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", userId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserHasRight() {

    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.findOne(program1Id)).willReturn(program1);
    given(facilityRepository.findOne(homeFacilityId)).willReturn(homeFacility);

    ResultDto<Boolean> response = new ResultDto<>();
    response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program1Id)
        .queryParam("facilityId", homeFacilityId)
        .pathParam("id", userId)
        .when()
        .get(HAS_RIGHT_URL)
        .then()
        .statusCode(200)
        .extract().as(response.getClass());

    assertTrue(response.getResult());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestGetUserHasRightWithMissingFacility() {

    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.findOne(program2Id)).willReturn(program2);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program2Id)
        .pathParam("id", userId)
        .when()
        .get(HAS_RIGHT_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserPrograms() throws RightTypeException {

    given(userRepository.findOne(userId)).willReturn(user1);

    Program[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", userId)
        .when()
        .get(PROGRAMS_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertThat(response.length, is(1));
    assertEquals(program1, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getUserSupervisedFacilitiesShouldReturnOk() throws RightTypeException {

    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.findOne(program2Id)).willReturn(program2);

    Facility[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program2Id)
        .pathParam("id", userId)
        .when()
        .get(SUPERVISED_FACILITIES_URL)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(response.length, is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getUserSupervisedFacilitiesShouldReturnNotFoundForNonExistingUser()
      throws RightTypeException {

    given(userRepository.findOne(userId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program2Id)
        .pathParam("id", userId)
        .when()
        .get(SUPERVISED_FACILITIES_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getUserSupervisedFacilitiesShouldReturnBadRequestForNonExistingUuid()
      throws RightTypeException {

    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.findOne(program1Id)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program1Id)
        .pathParam("id", userId)
        .when()
        .get(SUPERVISED_FACILITIES_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Ignore
  @Test
  public void shouldFindUsers() {

    given(userService.searchUsers(user1.getUsername(), user1.getFirstName(), user1.getLastName(),
        user1.getHomeFacility(), user1.isActive(), user1.isVerified()))
        .willReturn(singletonList(user1));

    UserDto[] response = restAssured
        .given()
        .queryParam(USERNAME, user1.getUsername())
        .queryParam(FIRST_NAME, user1.getFirstName())
        .queryParam(LAST_NAME, user1.getLastName())
        .queryParam(HOME_FACILITY, user1.getHomeFacility())
        .queryParam(ACTIVE, user1.isActive())
        .queryParam(VERIFIED, user1.isVerified())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    assertEquals(1, response.length);
    for (UserDto userDto : response) {
      assertEquals(
          userDto.getUsername(),
          user1.getUsername());
      assertEquals(
          userDto.getFirstName(),
          user1.getFirstName());
      assertEquals(
          userDto.getLastName(),
          user1.getLastName());
      assertEquals(
          userDto.getHomeFacility().getId(),
          user1.getHomeFacility().getId());
      assertEquals(
          userDto.isActive(),
          user1.isActive());
      assertEquals(
          userDto.isVerified(),
          user1.isVerified());
    }
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  /**
   * Creating requisition and auth users.
   */
  @Ignore
  @Test
  public void shouldCreateRequisitionAndAuthUsers() {
    User user = generateUser();

    User response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(user)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(User.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(response);

    User savedUser = userRepository.findOne(response.getId());
    assertNotNull(savedUser);

    assertEquals(user.getUsername(), savedUser.getUsername());
    assertEquals(user.getFirstName(), savedUser.getFirstName());
    assertEquals(user.getLastName(), savedUser.getLastName());
    assertEquals(user.getEmail(), savedUser.getEmail());
    assertEquals(user.getHomeFacility().getId(), savedUser.getHomeFacility().getId());
    assertEquals(user.isActive(), savedUser.isActive());
    assertEquals(user.isVerified(), savedUser.isVerified());

    AuthUserRequest authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertEquals(savedUser.getEmail(), authUser.getEmail());
    assertEquals(savedUser.getId(), authUser.getReferenceDataUserId());

    removeAuthUserByUsername(authUser.getUsername());
  }

  //TODO: This test should be updated when example email will be added to notification module
  @Ignore
  @Test
  public void shouldCreateRequisitionAndAuthUsersAndSendResetPasswordEmail() {
    User user = generateUser();

    User response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(user)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(User.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(response);

    User savedUser = userRepository.findOne(response.getId());
    assertNotNull(savedUser);

    assertEquals(user.getUsername(), savedUser.getUsername());
    assertEquals(user.getFirstName(), savedUser.getFirstName());
    assertEquals(user.getLastName(), savedUser.getLastName());
    assertEquals(user.getEmail(), savedUser.getEmail());
    assertEquals(user.getHomeFacility().getId(), savedUser.getHomeFacility().getId());
    assertEquals(user.isActive(), savedUser.isActive());
    assertEquals(user.isVerified(), savedUser.isVerified());

    AuthUserRequest authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertEquals(savedUser.getEmail(), authUser.getEmail());
    assertEquals(savedUser.getId(), authUser.getReferenceDataUserId());

    removeAuthUserByUsername(authUser.getUsername());
  }

  @Ignore
  @Test
  public void shouldResetPassword() {
    User savedUser = createUser();
    saveAuthUser(savedUser);

    AuthUserRequest authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertNull(authUser.getPassword());

    PasswordResetRequest passwordResetRequest =
        new PasswordResetRequest(savedUser.getUsername(), "test12345");

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(passwordResetRequest)
        .when()
        .post(RESET_PASSWORD_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertNotNull(authUser.getPassword());

    removeAuthUserByUsername(authUser.getUsername());
  }

  @Ignore
  @Test
  public void shouldChangePasswordIfValidResetTokenIsProvided() {
    User savedUser = createUser();
    saveAuthUser(savedUser);

    AuthUserRequest authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertNull(authUser.getPassword());

    UUID tokenId = passwordResetToken(authUser.getReferenceDataUserId());
    PasswordChangeRequest passwordChangeRequest =
        new PasswordChangeRequest(tokenId, authUser.getUsername(), "test12345");

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(passwordChangeRequest)
        .when()
        .post(CHANGE_PASSWORD_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertNotNull(authUser.getPassword());

    removeAuthUserByUsername(authUser.getUsername());
  }


  //need to be ignored atm
  @Ignore
  @Test
  public void shouldUpdateRequisitionAndAuthUsers() {
    User newUser = generateUser();
    UserDto newUserDto = new UserDto();
    newUser.export(newUserDto);
    UserDto user = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(newUserDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(user);

    User savedUser = userRepository.findOne(user.getId());
    assertNotNull(savedUser);

    user.setEmail(generateInstanceNumber() + "@mail.com");
    assertNotEquals(user.getEmail(), savedUser.getEmail());

    AuthUserRequest authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertEquals(savedUser.getEmail(), authUser.getEmail());
    assertEquals(savedUser.getId(), authUser.getReferenceDataUserId());

    User response = restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(user)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(User.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(response);

    savedUser = userRepository.findOne(user.getId());
    assertNotNull(savedUser);

    assertEquals(user.getUsername(), savedUser.getUsername());
    assertEquals(user.getFirstName(), savedUser.getFirstName());
    assertEquals(user.getLastName(), savedUser.getLastName());
    assertEquals(user.getEmail(), savedUser.getEmail());
    assertEquals(user.getHomeFacility(), savedUser.getHomeFacility());
    assertEquals(user.isActive(), savedUser.isActive());
    assertEquals(user.isVerified(), savedUser.isVerified());

    authUser = getAutUserByUsername(savedUser.getUsername());
    assertNotNull(authUser);

    assertEquals(savedUser.getEmail(), authUser.getEmail());
    assertEquals(savedUser.getId(), authUser.getReferenceDataUserId());

    removeAuthUserByUsername(authUser.getUsername());
  }

  private void saveAuthUser(User user) {
    AuthUserRequest userRequest = new AuthUserRequest();
    userRequest.setUsername(user.getUsername());
    userRequest.setEmail(user.getEmail());
    userRequest.setReferenceDataUserId(user.getId());

    final String url = baseUri + "?access_token=" + getToken();
    RestTemplate restTemplate = new RestTemplate();

    restTemplate.postForObject(url, userRequest, Object.class);
  }

  private UUID passwordResetToken(UUID referenceDataUserId) {
    final String url = baseUri + USER_API_STRING + "/passwordResetToken?userId="
        + referenceDataUserId + "&access_token=" + getToken();
    RestTemplate restTemplate = new RestTemplate();

    return restTemplate.postForObject(url, null, UUID.class);
  }

  private AuthUserRequest getAutUserByUsername(String username) {
    final String url = baseUri + USER_API_STRING + "/search/findOneByUsername?username=" + username
        + "&access_token=" + getToken();

    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(url, AuthUserRequest.class);
  }

  private void removeAuthUserByUsername(String username) {
    String url = baseUri + USER_API_STRING + "/search/findOneByUsername?username=" + username
        + "&access_token=" + getToken();

    RestTemplate restTemplate = new RestTemplate();
    Map map = restTemplate.getForObject(url, Map.class);
    String href = ((String) ((Map) ((Map) map.get("_links")).get("self")).get("href"));
    String id = href.split("users/")[1];

    url = baseUri + USER_API_STRING + id + "?access_token=" + getToken();
    restTemplate.delete(url);
  }

  private User createUser() {
    return userRepository.save(generateUser());
  }

  private User generateUser() {
    Integer instanceNumber = generateInstanceNumber();
    homeFacility = generateFacility(HOME_FACILITY_CODE);
    return new UserBuilder("kota" + instanceNumber,
        "Ala" + instanceNumber,
        "ma" + instanceNumber,
        instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setHomeFacility(homeFacility)
        .setVerified(true)
        .setActive(true)
        .createUser();
  }

  private Facility generateFacility(String facilityCode) {
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility(facilityCode);
    homeFacilityId = UUID.randomUUID();
    facility.setId(homeFacilityId);
    facility.setType(facilityType);
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    facility.setGeographicZone(geographicZone);
    Integer instanceNumber = +generateInstanceNumber();
    facility.setName("FacilityName" + instanceNumber);
    facility.setDescription("FacilityDescription" + instanceNumber);
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel" + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone" + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType" + generateInstanceNumber());
    return facilityType;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  private void assignUserRoles(User user) throws RightTypeException, RoleException,
      RoleAssignmentException {

    Right adminRight = Right.newRight("adminRight", RightType.GENERAL_ADMIN);
    adminRole = Role.newRole("adminRole", adminRight);
    adminRoleId = UUID.randomUUID();
    adminRole.setId(adminRoleId);

    supervisionRight = Right.newRight(SUPERVISION_RIGHT_NAME, RightType.SUPERVISION);
    supervisionRightId = UUID.randomUUID();
    supervisionRole = Role.newRole("supervisionRole", supervisionRight);
    supervisionRoleId = UUID.randomUUID();
    supervisionRole.setId(supervisionRoleId);
    program1 = new Program(PROGRAM1_CODE);
    program1Id = UUID.randomUUID();
    program2 = new Program(PROGRAM2_CODE);
    program2Id = UUID.randomUUID();
    supervisionRightId = UUID.randomUUID();
    supervisionRight.setId(supervisionRightId);
    supervisoryNode = SupervisoryNode.newSupervisoryNode(SUPERVISORY_NODE_CODE,
        generateFacility("F1"));
    RequisitionGroup supervisionGroup = new RequisitionGroup("SGC", "SGN", supervisoryNode);
    supervisionGroup.setMemberFacilities(Sets.newHashSet(generateFacility("F2"),
        generateFacility("F3")));
    RequisitionGroupProgramSchedule supervisionGroupProgramSchedule =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            supervisionGroup, program2, new ProcessingSchedule("PS1", "Schedule 1"), false);
    supervisionGroup.setRequisitionGroupProgramSchedules(
        Collections.singletonList(supervisionGroupProgramSchedule));
    supervisoryNode.setRequisitionGroup(supervisionGroup);

    Right fulfillmentRight = Right.newRight("fulfillmentRight", RightType.ORDER_FULFILLMENT);
    fulfillmentRole = Role.newRole("fulfillmentRole", fulfillmentRight);
    fulfillmentRoleId = UUID.randomUUID();
    fulfillmentRole.setId(fulfillmentRoleId);
    FacilityType warehouseType = new FacilityType("warehouse");
    warehouse = new Facility(WAREHOUSE_CODE);
    warehouse.setType(warehouseType);
    warehouse.setGeographicZone(generateGeographicZone(generateGeographicLevel()));
    warehouse.setActive(true);
    warehouse.setEnabled(true);

    DirectRoleAssignment roleAssignment1 = new DirectRoleAssignment(adminRole, user);
    SupervisionRoleAssignment roleAssignment2 = new SupervisionRoleAssignment(supervisionRole,
        user, program1);
    SupervisionRoleAssignment roleAssignment3 = new SupervisionRoleAssignment(supervisionRole,
        user, program2, supervisoryNode);
    FulfillmentRoleAssignment roleAssignment4 = new FulfillmentRoleAssignment(fulfillmentRole,
        user, warehouse);

    user.assignRoles(roleAssignment1, roleAssignment2, roleAssignment3, roleAssignment4);
  }
}
