package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
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
import org.openlmis.referencedata.util.AuthUserRequest;
import org.openlmis.referencedata.util.PasswordChangeRequest;
import org.openlmis.referencedata.util.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
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

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;
  
  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  private User user1;

  private static Integer currentInstanceNumber = 0;
  private Program program1;
  private Program program2;

  @Before
  public void setUp() throws RoleException, RoleAssignmentException, RightTypeException {
    user1 = generateUser();
    assignUserRoles(user1);
    userRepository.save(user1);

    createUser();
  }

  @Test
  public void shouldGetAllUsers() {

    UserDto[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    List<UserDto> users = Arrays.asList(response);
    assertThat(users.size(), is(3));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUser() {

    UserDto userDto = new UserDto();
    user1.export(userDto);

    UserDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", user1.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertEquals(userDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonExistingUser() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", UUID.randomUUID())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSaveNewUserOnPut() {

    User newUser = generateUser();
    UserDto newUserDto = new UserDto();
    newUser.export(newUserDto);

    UserDto response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(newUserDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertEquals(newUserDto, response);
    User storedUser = userRepository.findOneByUsername(newUserDto.getUsername());
    assertEquals(newUser, storedUser);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSaveExistingUserOnPut() {

    UserDto userDto = new UserDto();
    user1.export(userDto);
    userDto.setFirstName("Updated");

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
    User storedUser = userRepository.findOne(user1.getId());
    assertEquals("Updated", storedUser.getFirstName());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSaveUserForRoleAssignmentWithoutRole() {

    UserDto userDto = new UserDto();
    user1.export(userDto);
    userDto.setRoleAssignments(Sets.newHashSet(new RoleAssignmentDto()));

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(userDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSaveUserWithAddedRoles() throws RightTypeException, RoleException {

    Right adminRight2 = Right.newRight("adminRight2", RightType.GENERAL_ADMIN);
    rightRepository.save(adminRight2);
    Role adminRole2 = Role.newRole("adminRole2", adminRight2);
    roleRepository.save(adminRole2);
    DirectRoleAssignment addedRoleAssignment = new DirectRoleAssignment(adminRole2);
    user1.assignRoles(addedRoleAssignment);

    UserDto userDto = new UserDto();
    user1.export(userDto);

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
    User storedUser = userRepository.findOne(user1.getId());
    assertTrue(storedUser.getRoleAssignments().contains(addedRoleAssignment));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSaveUserWithRemovedRoles() {

    UserDto userDto = new UserDto();
    user1.export(userDto);

    userDto.setRoleAssignments(Sets.newHashSet());

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
    User storedUser = userRepository.findOne(user1.getId());
    assertTrue(storedUser.getRoleAssignments().isEmpty());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteUser() {

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", user1.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertFalse(userRepository.exists(user1.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNotExistingUser() {

    userRepository.delete(user1);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", user1.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnTrueIfUserHasRight() {

    Boolean response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("rightName", SUPERVISION_RIGHT_NAME)
        .queryParam("programCode", PROGRAM2_CODE)
        .queryParam("supervisoryNodeCode", SUPERVISORY_NODE_CODE)
        .pathParam("id", user1.getId())
        .when()
        .get(HAS_RIGHT_URL)
        .then()
        .statusCode(200)
        .extract().as(Boolean.class);

    assertTrue(response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnFalseIfUserDoesNotHaveRight() {

    Boolean response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("rightName", SUPERVISION_RIGHT_NAME)
        .queryParam("programCode", "DOES_NOT_EXIST")
        .queryParam("supervisoryNodeCode", "DOES_NOT_EXIST")
        .pathParam("id", user1.getId())
        .when()
        .get(HAS_RIGHT_URL)
        .then()
        .statusCode(200)
        .extract().as(Boolean.class);

    assertFalse(response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotCheckIfUserHasRightForNonExistingUser() {

    userRepository.delete(user1);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("rightName", SUPERVISION_RIGHT_NAME)
        .queryParam("programCode", PROGRAM2_CODE)
        .queryParam("supervisoryNodeCode", SUPERVISORY_NODE_CODE)
        .pathParam("id", user1.getId())
        .when()
        .get(HAS_RIGHT_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserHomeFacilityPrograms() throws RightTypeException {

    Program[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", user1.getId())
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
  public void shouldGetUserSupervisoryPrograms() throws RightTypeException {

    Program[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam("forHomeFacility", false)
        .pathParam("id", user1.getId())
        .when()
        .get(PROGRAMS_URL)
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertThat(response.length, is(1));
    assertEquals(program2, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetUserProgramsForNonExistingUser() {

    userRepository.delete(user1);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", user1.getId())
        .when()
        .get(PROGRAMS_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserSupervisedFacilities() throws RightTypeException {

    Facility[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", user1.getId())
        .when()
        .get(SUPERVISED_FACILITIES_URL)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(response.length, is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetUserSupervisedFacilitiesForNonExistingUser() {

    userRepository.delete(user1);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", user1.getId())
        .when()
        .get(SUPERVISED_FACILITIES_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindUsers() {
    UserDto[] response = restAssured
        .given()
        .queryParam(USERNAME, user1.getUsername())
        .queryParam(FIRST_NAME, user1.getFirstName())
        .queryParam(LAST_NAME, user1.getLastName())
        .queryParam(HOME_FACILITY, user1.getHomeFacility().getId())
        .queryParam(ACTIVE, user1.isActive())
        .queryParam(VERIFIED, user1.isVerified())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
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

    String url = "http://auth:8080/api/users?access_token=" + getToken();
    RestTemplate restTemplate = new RestTemplate();

    restTemplate.postForObject(url, userRequest, Object.class);
  }

  private UUID passwordResetToken(UUID referenceDataUserId) {
    String url = "http://auth:8080/api/users/passwordResetToken?userId=" + referenceDataUserId
        + "&access_token=" + getToken();
    RestTemplate restTemplate = new RestTemplate();

    return restTemplate.postForObject(url, null, UUID.class);
  }

  private AuthUserRequest getAutUserByUsername(String username) {
    String url = "http://auth:8080/api/users/search/findOneByUsername?username=" + username
        + "&access_token=" + getToken();

    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(url, AuthUserRequest.class);
  }

  private void removeAuthUserByUsername(String username) {
    String url = "http://auth:8080/api/users/search/findOneByUsername?username=" + username
        + "&access_token=" + getToken();

    RestTemplate restTemplate = new RestTemplate();
    Map map = restTemplate.getForObject(url, Map.class);
    String href = ((String) ((Map) ((Map) map.get("_links")).get("self")).get("href"));
    String id = href.split("users/")[1];

    url = "http://auth:8080/api/users/" + id + "?access_token=" + getToken();
    restTemplate.delete(url);
  }

  private User createUser() {
    return userRepository.save(generateUser());
  }

  private User generateUser() {
    Integer instanceNumber = generateInstanceNumber();
    return new UserBuilder("kota" + instanceNumber,
        "Ala" + instanceNumber,
        "ma" + instanceNumber,
        instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setHomeFacility(generateFacility())
        .setVerified(true)
        .setActive(true)
        .createUser();
  }

  private Facility generateFacility() {
    Integer instanceNumber = +generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility();
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setCode("FacilityCode" + instanceNumber);
    facility.setName("FacilityName" + instanceNumber);
    facility.setDescription("FacilityDescription" + instanceNumber);
    facility.setActive(true);
    facility.setEnabled(true);
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

  private void assignUserRoles(User user) throws RightTypeException, RoleException,
      RoleAssignmentException {

    Right adminRight = Right.newRight("adminRight", RightType.GENERAL_ADMIN);
    rightRepository.save(adminRight);
    Role adminRole = Role.newRole("adminRole", adminRight);
    roleRepository.save(adminRole);

    Right supervisionRight = Right.newRight(SUPERVISION_RIGHT_NAME, RightType.SUPERVISION);
    rightRepository.save(supervisionRight);
    Role supervisionRole = Role.newRole("supervisionRole", supervisionRight);
    roleRepository.save(supervisionRole);
    program1 = new Program(PROGRAM1_CODE);
    programRepository.save(program1);
    program2 = new Program(PROGRAM2_CODE);
    programRepository.save(program2);
    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode(SUPERVISORY_NODE_CODE,
        generateFacility());
    supervisoryNodeRepository.save(supervisoryNode);
    RequisitionGroup supervisionGroup = RequisitionGroup.newRequisitionGroup("supervisionGroup",
        supervisoryNode);
    supervisionGroup.setMemberFacilities(Arrays.asList(generateFacility(), generateFacility()));
    requisitionGroupRepository.save(supervisionGroup);
    supervisoryNode.setRequisitionGroup(supervisionGroup);
    supervisoryNodeRepository.save(supervisoryNode);

    Right fulfillmentRight = Right.newRight("fulfillmentRight", RightType.ORDER_FULFILLMENT);
    rightRepository.save(fulfillmentRight);
    Role fulfillmentRole = Role.newRole("fulfillmentRole", fulfillmentRight);
    roleRepository.save(fulfillmentRole);
    FacilityType warehouseType = new FacilityType("warehouse");
    facilityTypeRepository.save(warehouseType);
    Facility warehouse = new Facility();
    warehouse.setCode("W1");
    warehouse.setType(warehouseType);
    warehouse.setGeographicZone(generateGeographicZone(generateGeographicLevel()));
    warehouse.setActive(true);
    warehouse.setEnabled(true);
    facilityRepository.save(warehouse);

    DirectRoleAssignment roleAssignment1 = new DirectRoleAssignment(adminRole);
    SupervisionRoleAssignment roleAssignment2 = new SupervisionRoleAssignment(supervisionRole,
        program1);
    SupervisionRoleAssignment roleAssignment3 = new SupervisionRoleAssignment(supervisionRole,
        program2, supervisoryNode);
    FulfillmentRoleAssignment roleAssignment4 = new FulfillmentRoleAssignment(fulfillmentRole,
        warehouse);

    user.assignRoles(roleAssignment1, roleAssignment2, roleAssignment3, roleAssignment4);
  }
}
