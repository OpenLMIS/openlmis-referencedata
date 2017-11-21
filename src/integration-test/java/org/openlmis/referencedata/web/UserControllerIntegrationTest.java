/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.web;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.util.messagekeys.UserMessageKeys.ERROR_EMAIL_DUPLICATED;
import static org.openlmis.referencedata.util.messagekeys.UserMessageKeys.ERROR_FIRSTNAME_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.UserMessageKeys.ERROR_LASTNAME_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.UserMessageKeys.ERROR_USERNAME_DUPLICATED;
import static org.openlmis.referencedata.util.messagekeys.UserMessageKeys.ERROR_USERNAME_INVALID;
import static org.openlmis.referencedata.util.messagekeys.UserMessageKeys.ERROR_USERNAME_REQUIRED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.referencedata.PageImplRepresentation;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.dto.DetailedRoleAssignmentDto;
import org.openlmis.referencedata.dto.NamedResource;
import org.openlmis.referencedata.dto.ResultDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.UserSearchParams;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.UserSearchParamsDataBuilder;
import org.openlmis.referencedata.util.messagekeys.RightMessageKeys;
import org.openlmis.referencedata.utils.AuditLogHelper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class UserControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/users";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String RIGHT_SEARCH_URL = RESOURCE_URL + "/rightSearch";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ROLE_ASSIGNMENTS_URL = ID_URL + "/roleAssignments";
  private static final String HAS_RIGHT_URL = ID_URL + "/hasRight";
  private static final String PROGRAMS_URL = ID_URL + "/programs";
  private static final String SUPPORTED_PROGRAMS_URL = ID_URL + "/supportedPrograms";
  private static final String SUPERVISED_FACILITIES_URL = ID_URL + "/supervisedFacilities";
  private static final String FULFILLMENT_FACILITIES_URL = ID_URL + "/fulfillmentFacilities";
  private static final String PERMISSION_STRINGS_URL = ID_URL + "/permissionStrings";
  private static final String FACILITIES_URL = ID_URL + "/facilities";
  private static final String USERNAME = "username";
  private static final String TIMEZONE = "UTC";
  private static final String SUPERVISION_RIGHT_NAME = "supervisionRight";
  private static final String PROGRAM1_CODE = "P1";
  private static final String PROGRAM2_CODE = "P2";
  private static final String SUPERVISORY_NODE_CODE = "SN1";
  private static final String WAREHOUSE_CODE = "W1";
  private static final String HOME_FACILITY_CODE = "HF1";
  private static final String RIGHT_ID_STRING = "rightId";
  private static final String PROGRAM_ID_STRING = "programId";
  private static final String SUPERVISORY_NODE_ID_STRING = "supervisoryNodeId";
  private static final String WAREHOUSE_ID_STRING = "warehouseId";
  private static final String ADMIN_RIGHT_NAME = "adminRight";

  private static final UUID RIGHT_ID = UUID.randomUUID();
  private static final UUID SUPERVISORY_NODE_ID = UUID.randomUUID();
  private static final UUID PROGRAM_ID = UUID.randomUUID();
  private static final UUID WAREHOUSE_ID = UUID.randomUUID();
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String ID = "id";

  private User user1;
  private User user2;
  private UUID userId;
  private Facility homeFacility;
  private UUID homeFacilityId;

  private static Integer currentInstanceNumber = 0;
  private Role adminRole;
  private UUID adminRoleId;
  private Right supervisionRight;
  private UUID supervisionRightId;
  private Right fulfillmentRight;
  private UUID fulfillmentRightId;
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
  private DirectRoleAssignment roleAssignment1;
  private SupervisionRoleAssignment roleAssignment2;
  private SupervisionRoleAssignment roleAssignment3;
  private FulfillmentRoleAssignment roleAssignment4;

  /**
   * Constructor for test class.
   */
  public UserControllerIntegrationTest() {
    homeFacilityId = UUID.randomUUID();
    homeFacility = new FacilityDataBuilder().withCode(HOME_FACILITY_CODE).build();
    homeFacility.setId(homeFacilityId);

    userId = UUID.randomUUID();
    user1 = generateUser();
    user1.setId(userId);
    assignUserRoles(user1);

    user2 = generateUser();
    user2.setId(UUID.randomUUID());
  }

  @Test
  public void shouldGetAllUsers() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    UserSearchParams queryMap = new UserSearchParams();
    given(userService.searchUsersById(eq(queryMap), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(user1, generateUser())));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertThat(response.getContent().size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUsersByQueryParamsAndPaginate() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    String uuidOne = UUID.randomUUID().toString();
    String uuidTwo = UUID.randomUUID().toString();
    Set<String> ids = new HashSet<>(2);
    ids.add(uuidOne);
    ids.add(uuidTwo);

    UserSearchParams userSearchParams = new UserSearchParams(ids);
    given(userService.searchUsersById(eq(userSearchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(Lists.newArrayList(user1, generateUser()), null, 4));

    PageImplRepresentation response = restAssured
        .given()
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 2)
        .queryParam(ID, uuidOne)
        .queryParam(ID, uuidTwo)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(2, response.getContent().size());
    assertEquals(4, response.getTotalElements());
    assertEquals(2, response.getTotalPages());
    assertEquals(2, response.getNumberOfElements());
    assertEquals(2, response.getSize());
    assertEquals(0, response.getNumber());
    assertThat(response.getContent().size(), is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetAllUsersIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldGetUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    when(roleAssignmentRepository.findByUser(any(UUID.class))).thenReturn(Collections.emptySet());

    UserDto userDto = new UserDto();
    user1.export(userDto);

    UserDto response = getUser()
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertEquals(userDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);
    when(roleAssignmentRepository.findByUser(any(UUID.class))).thenReturn(Collections.emptySet());

    getUser()
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUserIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUser()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUsersFullRoleAssignments() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    DetailedRoleAssignmentDto[] response = getUsersFullRoleAssignments()
        .then()
        .statusCode(200)
        .extract().as(DetailedRoleAssignmentDto[].class);

    List<DetailedRoleAssignmentDto> actual = Lists.newArrayList(response);
    assertEquals(user1.getRoleAssignments().size(), actual.size());

    assertContainsRoleAssignment(actual, roleAssignment1);
    assertContainsRoleAssignment(actual, roleAssignment2);
    assertContainsRoleAssignment(actual, roleAssignment3);
    assertContainsRoleAssignment(actual, roleAssignment4);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUsersFullRoleAssignmentsWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    getUsersFullRoleAssignments()
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUsersFullRoleAssignmentsIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUsersFullRoleAssignments()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenGettingFullRoleAssignmentsForNotExistingUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.findOne(userId)).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(ROLE_ASSIGNMENTS_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    UserDto userDto = new UserDto();

    UserDto response = putUser(userDto)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertEquals(userDto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutUserIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = putUser(null)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutUserIfEmailIsInUse() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    User user = new User();
    user.setId(UUID.randomUUID());
    given(userRepository.findOneByEmail(user1.getEmail())).willReturn(user);

    String messageKey = putUser(null)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_EMAIL_DUPLICATED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutUserIfUsernameIsInUse() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    User user = new User();
    user.setId(UUID.randomUUID());
    given(userRepository.findOneByUsername(user1.getUsername())).willReturn(user);

    String messageKey = putUser(null)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_USERNAME_DUPLICATED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutUserIfUsernameIsNull() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    user1.setUsername(null);
    String messageKey = putUser(null)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_USERNAME_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldRejectPutUserIfUsernameIsInvalid() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    user1.setUsername("bad:name");
    String messageKey = putUser(null)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_USERNAME_INVALID)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotRejectPutUserIfEmailIsNull() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    user1.setEmail(null);
    putUser(null)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPutUserIfFirstNameIsNull() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    user1.setFirstName(null);
    String messageKey = putUser(null)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_FIRSTNAME_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldRejectPutUserIfLastNameIsNull() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    user1.setLastName(null);
    String messageKey = putUser(null)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(ERROR_LASTNAME_REQUIRED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldDeleteUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    deleteUser()
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteUserWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    deleteUser()
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteUserIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = deleteUser()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserHasRight() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(facilityRepository.exists(homeFacilityId)).willReturn(true);

    ResultDto<Boolean> response = new ResultDto<>();
    response = getUserHasRight()
        .then()
        .statusCode(200)
        .extract().as(response.getClass());

    assertTrue(response.getResult());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserHasRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    given(facilityRepository.exists(homeFacilityId)).willReturn(true);

    getUserHasRight()
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUserHasRightIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUserHasRight()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBadRequestGetUserHasRightWithMissingFacility() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.exists(userId)).willReturn(true);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.exists(program2Id)).willReturn(true);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldGetUserPrograms() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    Program[] response = getUserPrograms()
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertThat(response.length, is(1));
    assertEquals(program1, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserProgramsWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    getUserPrograms()
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUserProgramsIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUserPrograms()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserSupportedPrograms() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.exists(userId)).willReturn(true);

    Program[] response = getUserSupportedPrograms()
        .then()
        .statusCode(200)
        .extract().as(Program[].class);

    assertThat(response.length, is(1));
    assertEquals(program1, response[0]);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUserSupportedProgramsIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUserSupportedPrograms()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserSupportedProgramsWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);
    given(userRepository.exists(userId)).willReturn(true);

    getUserSupportedPrograms()
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfNoUserExistWhenGetSupportedPrograms() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);
    given(userRepository.exists(userId)).willReturn(false);

    getUserSupportedPrograms()
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserSupervisedFacilities() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    Facility[] response = getUserSupervisedFacilities()
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(response.length, is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserSupervisedFacilitiesWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    Facility[] response = getUserSupervisedFacilities()
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(response.length, is(2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUserSupervisedFacilitiesIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUserSupervisedFacilities()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getUserSupervisedFacilitiesShouldReturnNotFoundForNonExistingUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.findOne(userId)).willReturn(null);
    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void getUserSupervisedFacilitiesShouldReturnBadRequestForNonExistingUuid() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.findOne(program1Id)).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program1Id)
        .pathParam("id", userId)
        .when()
        .get(SUPERVISED_FACILITIES_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserFulfillmentFacilities() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    Facility[] response = getUserFulfillmentFacilities()
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(response.length, is(1));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserFulfillmentFacilitiesWithNoRightIfUserRequestsTheirOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    getUserFulfillmentFacilities()
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetUserFulfillmentFacilitiesIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUserFulfillmentFacilities()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenGettingFulfillmentFacilitiesWithIncorrectRight() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(fulfillmentRightId)).willReturn(null);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, fulfillmentRightId)
        .pathParam("id", userId)
        .when()
        .get(FULFILLMENT_FACILITIES_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindUsers() throws JsonProcessingException {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    UserSearchParams searchParams = new UserSearchParamsDataBuilder()
        .withUsername(user1.getUsername())
        .withExtraData(Collections.singletonMap("color", "orange"))
        .build();

    given(userService.searchUsers(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(singletonList(user1)));

    PageImplRepresentation response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(searchParams)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPaginateFindUsers() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    UserSearchParams searchParams = new UserSearchParamsDataBuilder().build();

    User user2 = generateUser();
    user2.setId(UUID.randomUUID());
    assignUserRoles(user2);

    given(userService.searchUsers(eq(searchParams), any(Pageable.class)))
        .willReturn(Pagination.getPage(asList(user1), null, 2));

    PageImplRepresentation response = restAssured
        .given()
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 1)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(searchParams)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(1, response.getContent().size());
    assertEquals(2, response.getTotalElements());
    assertEquals(2, response.getTotalPages());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(1, response.getSize());
    assertEquals(0, response.getNumber());
    assertTrue(response.isFirst());
    assertFalse(response.isLast());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectFindUsersIfUserHasNoRight() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    Map<String, Object> queryMap = new HashMap<>();
    queryMap.put(USERNAME, user1.getUsername());

    String messageKey = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(queryMap)
        .when()
        .post(SEARCH_URL)
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  /**
   * Creating users.
   */
  @Test
  public void shouldCreateUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    when(roleAssignmentRepository.findByUser(any(UUID.class))).thenReturn(Collections.emptySet());

    User user = generateUser();
    UserDto userDto = new UserDto();
    user.export(userDto);

    User response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(userDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(User.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(response);

    assertEquals(user.getUsername(), response.getUsername());
    assertEquals(user.getFirstName(), response.getFirstName());
    assertEquals(user.getLastName(), response.getLastName());
    assertEquals(user.getEmail(), response.getEmail());
    assertEquals(user.getHomeFacilityId(), response.getHomeFacilityId());
    assertEquals(user.isActive(), response.isActive());
    assertEquals(user.isVerified(), response.isVerified());
  }

  @Test
  public void shouldUpdateUser() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    when(roleAssignmentRepository.findByUser(any(UUID.class))).thenReturn(Collections.emptySet());

    User newUser = generateUser();
    UserDto newUserDto = new UserDto();
    newUser.export(newUserDto);

    UserDto user = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(newUserDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(user);

    String newEmail = generateInstanceNumber() + "@mail.com";
    assertNotEquals(newEmail, user.getEmail());
    user.setEmail(newEmail);

    User response = restAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(user)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(User.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertNotNull(response);

    assertEquals(user.getUsername(), response.getUsername());
    assertEquals(user.getFirstName(), response.getFirstName());
    assertEquals(user.getLastName(), response.getLastName());
    assertEquals(user.getEmail(), response.getEmail());
    assertEquals(user.getHomeFacilityId(), response.getHomeFacilityId());
    assertEquals(user.isActive(), response.isActive());
    assertEquals(user.isVerified(), response.isVerified());
  }

  @Test
  public void shouldSearchUsersBySupervisionRights() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    when(userService.rightSearch(RIGHT_ID, PROGRAM_ID, SUPERVISORY_NODE_ID, WAREHOUSE_ID))
        .thenReturn(newHashSet(user1, user2));

    UserDto[] users = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, RIGHT_ID)
        .queryParam(SUPERVISORY_NODE_ID_STRING, SUPERVISORY_NODE_ID)
        .queryParam(PROGRAM_ID_STRING, PROGRAM_ID)
        .queryParam(WAREHOUSE_ID_STRING, WAREHOUSE_ID)
        .when()
        .get(RIGHT_SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(UserDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertUsernames(users, user1.getUsername(), user2.getUsername());
    verify(userService).rightSearch(RIGHT_ID, PROGRAM_ID, SUPERVISORY_NODE_ID, WAREHOUSE_ID);
  }

  @Test
  public void shouldReturnForbiddenForRightSearchIfUserHasNoRights() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, RIGHT_ID)
        .when()
        .get(RIGHT_SEARCH_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verifyZeroInteractions(userService);
  }

  @Test
  public void shouldReturnBadRequestIfUserServiceThrowsException() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);
    when(userService.rightSearch(RIGHT_ID, null, null, null))
        .thenThrow(new ValidationMessageException(RightMessageKeys.ERROR_NOT_FOUND));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, RIGHT_ID)
        .when()
        .get(RIGHT_SEARCH_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(userService).rightSearch(RIGHT_ID, null, null, null);
  }

  @Test
  public void getUserAuditLogShouldReturnNotFoundIfUserDoesNotExist() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.notFound(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getUserAuditLogShouldReturnUnauthorizedIfUserDoesNotHaveRight() {
    doThrow(new UnauthorizedException(new Message("UNAUTHORIZED")))
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.findOne(any(UUID.class))).willReturn(null);

    AuditLogHelper.unauthorized(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetUserAuditLog() {
    doNothing()
        .when(rightService)
        .checkAdminRight(RightName.USERS_MANAGE_RIGHT);
    given(userRepository.exists(any(UUID.class))).willReturn(true);

    AuditLogHelper.ok(restAssured, getTokenHeader(), RESOURCE_URL);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getPermissionStringsShouldReturnOkIfServiceToken() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.exists(userId)).willReturn(true);
    given(rightAssignmentRepository.findByUser(userId))
        .willReturn(Sets.newHashSet(ADMIN_RIGHT_NAME));

    String[] response = getUsersPermissionStrings()
        .then()
        .statusCode(200)
        .extract().as(String[].class);

    Set<String> actual = Sets.newHashSet(response);
    assertEquals(1, actual.size());
    assertEquals(ADMIN_RIGHT_NAME, actual.iterator().next());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getPermissionStringsShouldReturnOkIfUserTokenAndUserRequestsOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    given(userRepository.exists(userId)).willReturn(true);
    given(rightAssignmentRepository.findByUser(userId))
        .willReturn(Sets.newHashSet(ADMIN_RIGHT_NAME));

    String[] response = getUsersPermissionStrings()
        .then()
        .statusCode(200)
        .extract().as(String[].class);

    Set<String> actual = Sets.newHashSet(response);
    assertEquals(1, actual.size());
    assertEquals(ADMIN_RIGHT_NAME, actual.iterator().next());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getPermissionStringsShouldReturnForbiddenIfUserTokenAndUserRequestsDifferentRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUsersPermissionStrings()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getPermissionStringsShouldReturnNotFoundIfUserDoesNotExist() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.exists(userId)).willReturn(false);

    getUsersPermissionStrings()
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getFacilitiesShouldReturnOkIfServiceToken() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.exists(userId)).willReturn(true);
    given(facilityRepository.findSupervisionFacilitiesByUser(userId))
        .willReturn(Sets.newHashSet(new NamedResource(homeFacilityId, homeFacility.getName())));

    NamedResource[] response = getUserFacilities()
        .then()
        .statusCode(200)
        .extract().as(NamedResource[].class);

    Set<NamedResource> actual = Sets.newHashSet(response);
    assertEquals(1, actual.size());
    NamedResource resource = actual.iterator().next();
    assertEquals(homeFacilityId, resource.getId());
    assertEquals(homeFacility.getName(), resource.getName());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getFacilitiesShouldReturnOkIfUserTokenAndUserRequestsOwnRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT, userId);

    given(userRepository.exists(userId)).willReturn(true);
    given(facilityRepository.findSupervisionFacilitiesByUser(userId))
        .willReturn(Sets.newHashSet(new NamedResource(homeFacilityId, homeFacility.getName())));

    NamedResource[] response = getUserFacilities()
        .then()
        .statusCode(200)
        .extract().as(NamedResource[].class);

    Set<NamedResource> actual = Sets.newHashSet(response);
    assertEquals(1, actual.size());
    NamedResource resource = actual.iterator().next();
    assertEquals(homeFacilityId, resource.getId());
    assertEquals(homeFacility.getName(), resource.getName());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getFacilitiesShouldReturnForbiddenIfUserTokenAndUserRequestsDifferentRecord() {
    mockUserHasNoRight(RightName.USERS_MANAGE_RIGHT);

    String messageKey = getUserFacilities()
        .then()
        .statusCode(403)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(messageKey, Matchers.is(equalTo(MESSAGEKEY_ERROR_UNAUTHORIZED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void getFacilitiesShouldReturnNotFoundIfUserDoesNotExist() {
    mockUserHasRight(RightName.USERS_MANAGE_RIGHT);

    given(userRepository.exists(userId)).willReturn(false);

    getUserFacilities()
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private Response getUsersPermissionStrings() {

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(PERMISSION_STRINGS_URL);
  }
  
  private Response getUserFacilities() {

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(FACILITIES_URL);
  }

  private Response getUser() {
    given(userRepository.findOne(userId)).willReturn(user1);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(ID_URL);
  }

  private Response getUsersFullRoleAssignments() {
    given(userRepository.findOne(userId)).willReturn(user1);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(ROLE_ASSIGNMENTS_URL);
  }

  private Response putUser(UserDto userDto) {
    if (userDto == null) {
      userDto = new UserDto();
    }
    user1.export(userDto);
    given(roleRepository.findOne(adminRoleId)).willReturn(adminRole);
    given(roleRepository.findOne(supervisionRoleId)).willReturn(supervisionRole);
    given(programRepository.findByCode(Code.code(PROGRAM1_CODE))).willReturn(program1);
    given(programRepository.findByCode(Code.code(PROGRAM2_CODE))).willReturn(program2);
    given(supervisoryNodeRepository.findByCode(SUPERVISORY_NODE_CODE)).willReturn(supervisoryNode);
    given(roleRepository.findOne(fulfillmentRoleId)).willReturn(fulfillmentRole);
    given(facilityRepository.findFirstByCode(WAREHOUSE_CODE)).willReturn(warehouse);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(userDto)
        .when()
        .put(RESOURCE_URL);
  }

  private Response deleteUser() {
    given(userRepository.findOne(userId)).willReturn(user1);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", userId)
        .when()
        .delete(ID_URL);
  }

  private Response getUserHasRight() {
    given(userRepository.exists(userId)).willReturn(true);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.exists(program1Id)).willReturn(true);
    given(programRepository.exists(program2Id)).willReturn(true);
    given(rightAssignmentRepository.existsByUserIdAndAndRightNameAndFacilityIdAndProgramId(
        userId, supervisionRight.getName(), homeFacilityId, program1Id)).willReturn(true);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program1Id)
        .queryParam("facilityId", homeFacilityId)
        .pathParam("id", userId)
        .when()
        .get(HAS_RIGHT_URL);
  }

  private Response getUserPrograms() {
    given(userRepository.exists(userId)).willReturn(true);
    given(programRepository.findSupervisionProgramsByUser(userId)).willReturn(
        Sets.newHashSet(program1));

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(PROGRAMS_URL);
  }
  
  private Response getUserSupportedPrograms() {
    given(programRepository.findHomeFacilitySupervisionProgramsByUser(userId)).willReturn(
        Sets.newHashSet(program1));

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", userId)
        .when()
        .get(SUPPORTED_PROGRAMS_URL);
  }

  private Response getUserSupervisedFacilities() {
    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(supervisionRightId)).willReturn(supervisionRight);
    given(programRepository.findOne(program2Id)).willReturn(program2);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, supervisionRightId)
        .queryParam(PROGRAM_ID_STRING, program2Id)
        .pathParam("id", userId)
        .when()
        .get(SUPERVISED_FACILITIES_URL);
  }

  private Response getUserFulfillmentFacilities() {
    given(userRepository.findOne(userId)).willReturn(user1);
    given(rightRepository.findOne(fulfillmentRightId)).willReturn(fulfillmentRight);

    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(RIGHT_ID_STRING, fulfillmentRightId)
        .pathParam("id", userId)
        .when()
        .get(FULFILLMENT_FACILITIES_URL);
  }

  private User generateUser() {
    Integer instanceNumber = generateInstanceNumber();
    return new UserBuilder("kota" + instanceNumber,
        "Ala" + instanceNumber,
        "ma" + instanceNumber,
        instanceNumber + "@mail.com")
        .setTimezone(TIMEZONE)
        .setHomeFacilityId(homeFacilityId)
        .setVerified(true)
        .setActive(true)
        .createUser();
  }

  private static Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  private void assignUserRoles(User user) {

    Right adminRight = Right.newRight(ADMIN_RIGHT_NAME, RightType.GENERAL_ADMIN);
    adminRole = Role.newRole("adminRole", adminRight);
    adminRoleId = UUID.randomUUID();
    adminRole.setId(adminRoleId);

    supervisionRight = Right.newRight(SUPERVISION_RIGHT_NAME, RightType.SUPERVISION);
    supervisionRightId = UUID.randomUUID();
    supervisionRole = Role.newRole("supervisionRole", supervisionRight);
    supervisionRoleId = UUID.randomUUID();
    supervisionRole.setId(supervisionRoleId);
    program1 = new Program(PROGRAM1_CODE);
    program1.setPeriodsSkippable(false);
    program1Id = UUID.randomUUID();
    program2 = new Program(PROGRAM2_CODE);
    program2.setPeriodsSkippable(false);
    program2Id = UUID.randomUUID();
    supervisionRightId = UUID.randomUUID();
    supervisionRight.setId(supervisionRightId);
    supervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(new FacilityDataBuilder().build())
        .withCode(SUPERVISORY_NODE_CODE)
        .build();
    RequisitionGroup supervisionGroup = new RequisitionGroup("SGC", "SGN", supervisoryNode);
    supervisionGroup.setMemberFacilities(newHashSet(
        new FacilityDataBuilder().build(),
        new FacilityDataBuilder().build()));
    addSupportedPrograms(supervisionGroup, program2);
    RequisitionGroupProgramSchedule supervisionGroupProgramSchedule =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            supervisionGroup, program2, new ProcessingSchedule("PS1", "Schedule 1"), false);
    supervisionGroup.setRequisitionGroupProgramSchedules(
        Collections.singletonList(supervisionGroupProgramSchedule));
    supervisoryNode.setRequisitionGroup(supervisionGroup);

    fulfillmentRight = Right.newRight("fulfillmentRight", RightType.ORDER_FULFILLMENT);
    fulfillmentRightId = UUID.randomUUID();
    fulfillmentRole = Role.newRole("fulfillmentRole", fulfillmentRight);
    fulfillmentRoleId = UUID.randomUUID();
    fulfillmentRole.setId(fulfillmentRoleId);
    FacilityType warehouseType = new FacilityType("warehouse");
    warehouse = new Facility(WAREHOUSE_CODE);
    warehouse.setType(warehouseType);
    warehouse.setGeographicZone(new GeographicZoneDataBuilder().build());
    warehouse.setActive(true);
    warehouse.setEnabled(true);

    roleAssignment1 = new DirectRoleAssignment(adminRole, user);
    roleAssignment2 = new SupervisionRoleAssignment(supervisionRole,
        user, program1);
    roleAssignment3 = new SupervisionRoleAssignment(supervisionRole,
        user, program2, supervisoryNode);
    roleAssignment4 = new FulfillmentRoleAssignment(fulfillmentRole,
        user, warehouse);

    user.assignRoles(roleAssignment1, roleAssignment2, roleAssignment3, roleAssignment4);
  }

  private void addSupportedPrograms(RequisitionGroup group, Program program) {
    group
        .getMemberFacilities()
        .forEach(facility -> facility
            .setSupportedPrograms(
                Sets.newHashSet(SupportedProgram.newSupportedProgram(facility, program, true))
            ));
  }

  private void assertContainsRoleAssignment(List<DetailedRoleAssignmentDto> dtos,
                                            RoleAssignment roleAssignment) {

    DetailedRoleAssignmentDto actual = new DetailedRoleAssignmentDto();

    if (roleAssignment instanceof SupervisionRoleAssignment) {
      ((SupervisionRoleAssignment) roleAssignment).export(actual);
    } else if (roleAssignment instanceof FulfillmentRoleAssignment) {
      ((FulfillmentRoleAssignment) roleAssignment).export(actual);
    } else {
      ((DirectRoleAssignment) roleAssignment).export(actual);
    }

    assertTrue(dtos.contains(actual));
  }

  private void assertUsernames(UserDto[] users, String... usernames) {
    assertThat(users, arrayWithSize(usernames.length));
    List<String> extractedNames = Arrays.stream(users)
        .map(UserDto::getUsername)
        .collect(Collectors.toList());
    assertThat(extractedNames, containsInAnyOrder(usernames));
  }
}
