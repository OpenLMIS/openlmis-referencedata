package org.openlmis.referencedata.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
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
import org.openlmis.referencedata.i18n.ExposedMessageSource;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.validation.BindingResult;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class UserControllerTest {

  private static final String ACCESS_TOKEN = "49c1e712-da50-4428-ae39-2d0409bd8059";

  @Mock
  private UserService service;

  @Mock
  private UserRepository repository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private ExposedMessageSource messageSource;

  @Mock
  private RightRepository rightRepository;

  private UserController controller;

  private String homeFacilityCode;
  private Facility homeFacility;
  private String user1UserName;
  private User user1;
  private UserDto user1Dto;
  private String user2UserName;
  private User user2;
  private UserDto user2Dto;
  private Set<User> users;
  private UUID userId;
  private UUID roleId;
  private Role adminRole1;
  private String supervisionRight1Name;
  private Right supervisionRight1;
  private Role supervisionRole1;
  private String programCode;
  private Program program1;
  private String supervisoryNodeCode;
  private SupervisoryNode supervisoryNode1;
  private String fulfillmentRight1Name;
  private Right fulfillmentRight1;
  private Role fulfillmentRole1;
  private String warehouseCode;
  private Facility warehouse1;

  /**
   * Constructor for test.
   */
  public UserControllerTest() throws RightTypeException, RoleException {
    initMocks(this);
    controller = new UserController(service, repository, roleRepository, rightRepository,
        programRepository, supervisoryNodeRepository, facilityRepository, messageSource);

    homeFacilityCode = "homeFacilityCode";
    homeFacility = new Facility("C1");
    user1UserName = "user1";
    user2UserName = "user2";
    user1 = new UserBuilder(user1UserName, "User", "1", "user1@openlmis.org")
        .setHomeFacility(homeFacility)
        .createUser();
    user2 = new UserBuilder(user2UserName, "User", "2", "user2@openlmis.org")
        .setHomeFacility(homeFacility)
        .createUser();
    users = Sets.newHashSet(user1, user2);

    user1Dto = new UserDto();
    user1.export(user1Dto);
    user1Dto.setHomeFacilityCode(homeFacilityCode);
    user2Dto = new UserDto();
    user2.export(user2Dto);
    user2Dto.setHomeFacilityCode(homeFacilityCode);

    userId = UUID.randomUUID();

    roleId = UUID.randomUUID();
    adminRole1 = Role.newRole("adminRole1", Right.newRight("adminRight1", RightType.GENERAL_ADMIN));
    adminRole1.setId(roleId);
    supervisionRight1Name = "supervisionRight1";
    supervisionRight1 = Right.newRight(supervisionRight1Name, RightType.SUPERVISION);
    supervisionRole1 = Role.newRole("supervisionRole1", supervisionRight1);
    supervisionRole1.setId(roleId);
    programCode = "P1";
    program1 = new Program(programCode);
    supervisoryNodeCode = "SN1";
    supervisoryNode1 = new SupervisoryNode();
    supervisoryNode1.setCode(supervisoryNodeCode);
    fulfillmentRight1Name = "fulfillmentRight1";
    fulfillmentRight1 = Right.newRight(fulfillmentRight1Name, RightType.ORDER_FULFILLMENT);
    fulfillmentRole1 = Role.newRole("fulfillmentRole1", fulfillmentRight1);
    fulfillmentRole1.setId(roleId);
    warehouseCode = "W1";
    warehouse1 = new Facility("C2");
    warehouse1.setCode(warehouseCode);
    warehouse1.setType(new FacilityType("warehouse"));
  }

  public void preparePostOrPut() {
    when(repository.findOne(userId)).thenReturn(user1);
    when(facilityRepository.findFirstByCode(homeFacilityCode)).thenReturn(homeFacility);
  }

  @Test
  public void shouldGetAllUsers() {
    //given
    Set<UserDto> expectedUserDtos = Sets.newHashSet(user1Dto, user2Dto);
    when(repository.findAll()).thenReturn(users);

    //when
    ResponseEntity responseEntity = controller.getAllUsers();
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<UserDto> userDtos = (Set<UserDto>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expectedUserDtos, userDtos);
  }

  @Test
  public void shouldGetUser() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    ResponseEntity responseEntity = controller.getUser(userId);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto userDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, userDto);
  }

  @Test
  public void shouldGetUserWithRoles() throws RightTypeException {
    //given
    DirectRoleAssignment roleAssignment1 = new DirectRoleAssignment(adminRole1, user1);
    SupervisionRoleAssignment roleAssignment2 = new SupervisionRoleAssignment(supervisionRole1,
        user1, program1);
    user1.assignRoles(roleAssignment1, roleAssignment2);
    when(repository.findOne(userId)).thenReturn(user1);

    UserDto expectedUserDto = new UserDto();
    user1.export(expectedUserDto);

    //when
    ResponseEntity responseEntity = controller.getUser(userId);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto userDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expectedUserDto, userDto);
  }

  @Test
  public void shouldNotGetNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.getUser(userId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldCreateNewUserOnPut() {
    //given
    preparePostOrPut();


    when(repository.findOne(userId)).thenReturn(null);
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    HttpStatus httpStatus = controller.saveUser(user1Dto, result, auth).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldUpdateExistingUserOnPut() {
    //given
    preparePostOrPut();

    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    HttpStatus httpStatus = controller.saveUser(user1Dto, result, auth).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldNotSaveUserForRoleAssignmentWithoutRole() throws RightTypeException {
    //given
    preparePostOrPut();

    user1Dto.setRoleAssignments(Sets.newHashSet(new RoleAssignmentDto()));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn("49c1e712-da50-4428-ae39-2d0409bd8059");

    //when
    HttpStatus httpStatus = controller.saveUser(user1Dto, result, auth).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.BAD_REQUEST));
  }

  @Test
  public void shouldSaveUserWithDirectRole() throws RightTypeException {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(adminRole1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    ResponseEntity responseEntity = controller.saveUser(user1Dto, result, auth);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto savedUserDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, savedUserDto);
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldSaveUserWithHomeFacilityRole() throws RightTypeException {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(supervisionRole1);
    when(programRepository.findByCode(Code.code(programCode))).thenReturn(program1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    roleAssignmentDto.setProgramCode(programCode);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);


    //when
    ResponseEntity responseEntity = controller.saveUser(user1Dto, result, auth);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto savedUserDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, savedUserDto);
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldSaveUserWithSupervisoryRole() throws RightTypeException {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(supervisionRole1);
    when(programRepository.findByCode(Code.code(programCode))).thenReturn(program1);
    when(supervisoryNodeRepository.findByCode(supervisoryNodeCode)).thenReturn(supervisoryNode1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    roleAssignmentDto.setProgramCode(programCode);
    roleAssignmentDto.setSupervisoryNodeCode(supervisoryNodeCode);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    ResponseEntity responseEntity = controller.saveUser(user1Dto, result, auth);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto savedUserDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, savedUserDto);
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldSaveUserWithFulfillmentRole() throws RightTypeException,
      RoleAssignmentException {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(fulfillmentRole1);
    when(facilityRepository.findFirstByCode(warehouseCode)).thenReturn(warehouse1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    roleAssignmentDto.setWarehouseCode(warehouseCode);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    ResponseEntity responseEntity = controller.saveUser(user1Dto, result, auth);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto savedUserDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, savedUserDto);
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldReplaceExistingUserRoles() throws RightTypeException {
    //given
    preparePostOrPut();

    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1));


    when(roleRepository.findOne(roleId)).thenReturn(adminRole1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    ResponseEntity responseEntity = controller.saveUser(user1Dto, result, auth);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto savedUserDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, savedUserDto);
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldDeleteExistingUserRoles() throws RightTypeException {
    //given
    preparePostOrPut();


    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);
    OAuth2Authentication auth = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails details = mock(OAuth2AuthenticationDetails.class);
    when(auth.getDetails()).thenReturn(details);
    when(details.getTokenValue()).thenReturn(ACCESS_TOKEN);

    //when
    ResponseEntity responseEntity = controller.saveUser(user1Dto, result, auth);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    UserDto savedUserDto = (UserDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(user1Dto, savedUserDto);
    verify(service).save(user1, ACCESS_TOKEN);
  }

  @Test
  public void shouldDeleteExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    HttpStatus httpStatus = controller.deleteUser(userId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NO_CONTENT));
    verify(repository).delete(userId);
  }

  @Test
  public void shouldNotDeleteNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.deleteUser(userId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
    verify(repository, never()).delete(userId);
  }

  @Test
  public void shouldNotCheckIfUserHasRightForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.checkIfUserHasRight(
        userId, UUID.randomUUID(), null, null, null).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldReturnTrueIfUserHasRight() throws RightTypeException {
    //given
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1));
    when(repository.findOne(userId)).thenReturn(user1);
    when(rightRepository.findOne(any(UUID.class))).thenReturn(supervisionRight1);
    when(programRepository.findOne(any(UUID.class))).thenReturn(program1);
    when(facilityRepository.findOne(any(UUID.class))).thenReturn(homeFacility);

    //when
    ResponseEntity responseEntity = controller.checkIfUserHasRight(userId, UUID.randomUUID(),
        UUID.randomUUID(), UUID.randomUUID(), null);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    BooleanResultDto booleanResultDto = (BooleanResultDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertTrue(booleanResultDto.getResult());
  }

  @Test
  public void shouldReturnFalseIfUserDoesNotHaveRight() throws RightTypeException {
    //given
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1, 
        supervisoryNode1));
    when(repository.findOne(userId)).thenReturn(user1);
    when(rightRepository.findOne(any(UUID.class))).thenReturn(fulfillmentRight1);
    when(facilityRepository.findOne(any(UUID.class))).thenReturn(warehouse1);

    //when
    ResponseEntity responseEntity = controller.checkIfUserHasRight(userId, UUID.randomUUID(),
        null, null, UUID.randomUUID());
    HttpStatus httpStatus = responseEntity.getStatusCode();
    BooleanResultDto booleanResultDto = (BooleanResultDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertFalse(booleanResultDto.getResult());
  }

  @Test
  public void shouldNotGetUserProgramsForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.getUserPrograms(userId, true).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldGetUserHomeFacilityPrograms() throws RightTypeException {
    //given
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1));
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    ResponseEntity responseEntity = controller.getUserPrograms(userId, true);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<Program> homeFacilityPrograms = (Set<Program>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertThat(homeFacilityPrograms.size(), is(1));
    assertTrue(homeFacilityPrograms.contains(program1));
  }

  @Test
  public void shouldGetUserSupervisoryPrograms() throws RightTypeException {
    //given
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1, 
        supervisoryNode1));
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    ResponseEntity responseEntity = controller.getUserPrograms(userId, false);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<Program> supervisoryPrograms = (Set<Program>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertThat(supervisoryPrograms.size(), is(1));
    assertTrue(supervisoryPrograms.contains(program1));
  }

  @Test
  public void shouldNotGetUserSupervisedFacilitiesForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.getUserSupervisedFacilities(userId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldGetUserSupervisedFacilities() throws RightTypeException {
    //given
    RequisitionGroup supervisionGroup1 = new RequisitionGroup(
        "supervisionGroup1", "supervisionGroupName1", supervisoryNode1
    );
    supervisionGroup1.setMemberFacilities(Sets.newHashSet(new Facility("C1"), new Facility("C2")));
    supervisoryNode1.setRequisitionGroup(supervisionGroup1);
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1, 
        supervisoryNode1));
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    ResponseEntity responseEntity = controller.getUserSupervisedFacilities(userId);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<Facility> supervisedFacilities = (Set<Facility>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertThat(supervisedFacilities.size(), is(2));
  }

  @Test
  public void shouldGetUserFulfillmentFacilities()
      throws RightTypeException, RoleAssignmentException {
    //given
    FulfillmentRoleAssignment assignment1 =
        new FulfillmentRoleAssignment(fulfillmentRole1, user1, warehouse1);

    SupervisionRoleAssignment assignment2
        = new SupervisionRoleAssignment(supervisionRole1, user1, program1, supervisoryNode1);

    user1.assignRoles(assignment1);
    user1.assignRoles(assignment2);
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    ResponseEntity responseEntity = controller.getUserFulfillmentFacilities(userId);
    Set<Facility> facilities = (Set<Facility>) responseEntity.getBody();

    //then
    assertThat(facilities.size(), is(1));
  }
}
