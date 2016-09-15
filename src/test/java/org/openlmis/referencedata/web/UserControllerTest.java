package org.openlmis.referencedata.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class UserControllerTest {

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
  private RightRepository rightRepository;

  private UserController controller;

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
  private Role supervisionRole1;
  private String programCode;
  private Program program1;
  private String supervisoryNodeCode;
  private SupervisoryNode supervisoryNode1;
  private Role fulfillmentRole1;
  private String warehouseCode;
  private Facility warehouse1;

  /**
   * Constructor for test.
   */
  public UserControllerTest() throws RightTypeException, RoleException {
    initMocks(this);
    controller = new UserController(service, repository, roleRepository, rightRepository,
        programRepository, supervisoryNodeRepository, facilityRepository);

    user1UserName = "user1";
    user2UserName = "user2";
    user1 = User.newUser(user1UserName, "User", "1", "user1@openlmis.org", true, true);
    user2 = User.newUser(user2UserName, "User", "2", "user2@openlmis.org", true, true);
    users = Sets.newHashSet(user1, user2);

    user1Dto = new UserDto();
    user1.export(user1Dto);
    user2Dto = new UserDto();
    user2.export(user2Dto);

    userId = UUID.randomUUID();

    roleId = UUID.randomUUID();
    adminRole1 = Role.newRole("adminRole1", Right.newRight("adminRight1", RightType.GENERAL_ADMIN));
    adminRole1.setId(roleId);
    supervisionRole1 = Role.newRole("supervisionRole1", Right.newRight("supervisionRight1",
        RightType.SUPERVISION));
    supervisionRole1.setId(roleId);
    programCode = "P1";
    program1 = new Program(programCode);
    supervisoryNodeCode = "SN1";
    supervisoryNode1 = new SupervisoryNode();
    supervisoryNode1.setCode(supervisoryNodeCode);
    fulfillmentRole1 = Role.newRole("fulfillmentRole1", Right.newRight("fulfillmentRight1",
        RightType.ORDER_FULFILLMENT));
    fulfillmentRole1.setId(roleId);
    warehouseCode = "W1";
    warehouse1 = new Facility();
    warehouse1.setCode(warehouseCode);
    warehouse1.setType(new FacilityType("warehouse"));
  }

  public void preparePostOrPut() {
    when(repository.findOne(userId)).thenReturn(user1);
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
  public void shouldNotGetNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.getUser(userId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldCreateNewUserOnPost() {
    //given
    preparePostOrPut();

    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.saveUser(user1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.CREATED));
    //verify(repository).save(user1); TODO: until we determine what is a user's "identity"
  }

  //  @Test
  //  public void shouldUpdateUserOnPut() {
  //    //given
  //    preparePostOrPut();
  //
  //    user1Dto.setLastName("Updated");
  //    User updatedUser1 = User.newUser(user1Dto);
  //
  //    //when
  //    HttpStatus httpStatus = controller.updateUser(userId, user1Dto).getStatusCode();
  //
  //    //then
  //    assertThat(httpStatus, is(HttpStatus.OK));
  //    verify(repository).save(updatedUser1);
  //  }
  //
  //  @Test
  //  public void shouldCreateNewUserOnPut() {
  //    //given
  //    preparePostOrPut();
  //
  //    when(repository.findOne(userId)).thenReturn(null);
  //    user1Dto.setLastName("Updated");
  //    User updatedUser1 = User.newUser(user1Dto);
  //
  //    //when
  //    HttpStatus httpStatus = controller.updateUser(userId, user1Dto).getStatusCode();
  //
  //    //then
  //    assertThat(httpStatus, is(HttpStatus.OK));
  //    verify(repository).save(updatedUser1);
  //  }

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
  public void shouldGetAllUserRoles() throws RightTypeException {
    //given
    DirectRoleAssignment roleAssignment1 = new DirectRoleAssignment(adminRole1);
    SupervisionRoleAssignment roleAssignment2 = new SupervisionRoleAssignment(supervisionRole1,
        program1);
    user1.assignRoles(roleAssignment1, roleAssignment2);
    when(repository.findOne(userId)).thenReturn(user1);

    RoleAssignmentDto roleAssignment1Dto = new RoleAssignmentDto();
    RoleAssignmentDto roleAssignment2Dto = new RoleAssignmentDto();
    roleAssignment1.export(roleAssignment1Dto);
    roleAssignment2.export(roleAssignment2Dto);
    Set<RoleAssignmentDto> expectedRoleAssignmentDtos = Sets.newHashSet(roleAssignment1Dto,
        roleAssignment2Dto);

    //when
    ResponseEntity responseEntity = controller.getAllUserRoles(userId);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> roleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expectedRoleAssignmentDtos, roleAssignmentDtos);
  }

  @Test
  public void shouldNotGetAllUserRolesForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.getAllUserRoles(userId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldNotSaveUserRoleForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);
    Set<RoleAssignmentDto> roleAssignmentDtos = new HashSet<>();

    //when
    HttpStatus httpStatus = controller.saveUserRoles(userId, roleAssignmentDtos).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void shouldNotSaveUserRoleForRoleAssignmentWithoutRole() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);
    Set<RoleAssignmentDto> roleAssignmentDtos = Sets.newHashSet(new RoleAssignmentDto());

    //when
    HttpStatus httpStatus = controller.saveUserRoles(userId, roleAssignmentDtos).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.BAD_REQUEST));
  }

  @Test
  public void shouldSaveDirectUserRole() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleRepository.findOne(roleId)).thenReturn(adminRole1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    Set<RoleAssignmentDto> roleAssignmentDtos = Sets.newHashSet(roleAssignmentDto);

    //when
    ResponseEntity responseEntity = controller.saveUserRoles(userId, roleAssignmentDtos);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> savedRoleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity
        .getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(roleAssignmentDtos, savedRoleAssignmentDtos);
  }

  @Test
  public void shouldSaveHomeFacilityUserRole() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleRepository.findOne(roleId)).thenReturn(supervisionRole1);
    when(programRepository.findByCode(Code.code(programCode))).thenReturn(program1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    roleAssignmentDto.setProgramCode(programCode);
    Set<RoleAssignmentDto> roleAssignmentDtos = Sets.newHashSet(roleAssignmentDto);

    //when
    ResponseEntity responseEntity = controller.saveUserRoles(userId, roleAssignmentDtos);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> savedRoleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity
        .getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(roleAssignmentDtos, savedRoleAssignmentDtos);
  }

  @Test
  public void shouldSaveSupervisoryUserRole() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleRepository.findOne(roleId)).thenReturn(supervisionRole1);
    when(programRepository.findByCode(Code.code(programCode))).thenReturn(program1);
    when(supervisoryNodeRepository.findByCode(supervisoryNodeCode)).thenReturn(supervisoryNode1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    roleAssignmentDto.setProgramCode(programCode);
    roleAssignmentDto.setSupervisoryNodeCode(supervisoryNodeCode);
    Set<RoleAssignmentDto> roleAssignmentDtos = Sets.newHashSet(roleAssignmentDto);

    //when
    ResponseEntity responseEntity = controller.saveUserRoles(userId, roleAssignmentDtos);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> savedRoleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity
        .getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(roleAssignmentDtos, savedRoleAssignmentDtos);
  }

  @Test
  public void shouldSaveFulfillmentUserRole() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleRepository.findOne(roleId)).thenReturn(fulfillmentRole1);
    when(facilityRepository.findFirstByCode(warehouseCode)).thenReturn(warehouse1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    roleAssignmentDto.setWarehouseCode(warehouseCode);
    Set<RoleAssignmentDto> roleAssignmentDtos = Sets.newHashSet(roleAssignmentDto);

    //when
    ResponseEntity responseEntity = controller.saveUserRoles(userId, roleAssignmentDtos);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> savedRoleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity
        .getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(roleAssignmentDtos, savedRoleAssignmentDtos);
  }

  @Test
  public void shouldReplaceExistingUserRoles() throws RightTypeException {
    //given
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, program1));
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleRepository.findOne(roleId)).thenReturn(adminRole1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRoleId(roleId);
    Set<RoleAssignmentDto> roleAssignmentDtos = Sets.newHashSet(roleAssignmentDto);

    //when
    ResponseEntity responseEntity = controller.saveUserRoles(userId, roleAssignmentDtos);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> savedRoleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity
        .getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(roleAssignmentDtos, savedRoleAssignmentDtos);
  }

  @Test
  public void shouldDeleteExistingUserRoles() throws RightTypeException {
    //given
    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, program1));
    when(repository.findOne(userId)).thenReturn(user1);
    Set<RoleAssignmentDto> roleAssignmentDtos = new HashSet<>();

    //when
    ResponseEntity responseEntity = controller.saveUserRoles(userId, roleAssignmentDtos);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleAssignmentDto> savedRoleAssignmentDtos = (Set<RoleAssignmentDto>) responseEntity
        .getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(roleAssignmentDtos, savedRoleAssignmentDtos);
  }
}
