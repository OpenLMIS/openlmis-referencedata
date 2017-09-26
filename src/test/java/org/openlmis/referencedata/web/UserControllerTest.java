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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.RoleAssignmentDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleAssignmentRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.RightService;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.validate.UserValidator;
import org.springframework.validation.BindingResult;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
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
  private RightRepository rightRepository;

  @Mock
  private RightService rightService;

  @Mock
  private UserValidator validator;
  
  @Mock
  private RoleAssignmentRepository roleAssignmentRepository;

  @InjectMocks
  private UserController controller = new UserController();

  private UUID homeFacilityId;
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
  private UUID rightId;
  private Role adminRole1;
  private String supervisionRight1Name;
  private Right supervisionRight1;
  private Role supervisionRole1;
  private UUID programId;
  private Program program1;
  private UUID supervisoryNodeId;
  private SupervisoryNode supervisoryNode1;
  private String fulfillmentRight1Name;
  private Right fulfillmentRight1;
  private Role fulfillmentRole1;
  private UUID warehouseId;
  private Facility warehouse1;

  /**
   * Constructor for test.
   */
  public UserControllerTest() {
    initMocks(this);

    homeFacilityId = UUID.randomUUID();
    homeFacility = new Facility("C1");
    homeFacility.setId(homeFacilityId);
    user1UserName = "user1";
    user2UserName = "user2";
    user1 = new UserBuilder(user1UserName, "User", "1", "user1@openlmis.org")
        .setHomeFacilityId(homeFacilityId)
        .createUser();
    user2 = new UserBuilder(user2UserName, "User", "2", "user2@openlmis.org")
        .setHomeFacilityId(homeFacilityId)
        .createUser();
    users = Sets.newHashSet(user1, user2);

    user1Dto = new UserDto();
    user1.export(user1Dto);
    user1Dto.setHomeFacilityId(homeFacilityId);
    user2Dto = new UserDto();
    user2.export(user2Dto);
    user2Dto.setHomeFacilityId(homeFacilityId);

    userId = UUID.randomUUID();

    roleId = UUID.randomUUID();
    adminRole1 = Role.newRole("adminRole1", Right.newRight("adminRight1", RightType.GENERAL_ADMIN));
    adminRole1.setId(roleId);
    supervisionRight1Name = "supervisionRight1";
    supervisionRight1 = Right.newRight(supervisionRight1Name, RightType.SUPERVISION);
    supervisionRole1 = Role.newRole("supervisionRole1", supervisionRight1);
    supervisionRole1.setId(roleId);
    programId = UUID.randomUUID();
    program1 = new Program("P1");
    program1.setId(programId);
    supervisoryNodeId = UUID.randomUUID();
    supervisoryNode1 = new SupervisoryNode();
    supervisoryNode1.setId(supervisoryNodeId);
    fulfillmentRight1Name = "fulfillmentRight1";
    fulfillmentRight1 = Right.newRight(fulfillmentRight1Name, RightType.ORDER_FULFILLMENT);
    fulfillmentRole1 = Role.newRole("fulfillmentRole1", fulfillmentRight1);
    fulfillmentRole1.setId(roleId);
    warehouseId = UUID.randomUUID();
    warehouse1 = new Facility("C2");
    warehouse1.setId(warehouseId);
    warehouse1.setType(new FacilityType("warehouse"));
  }

  public void preparePostOrPut() {
    when(repository.findOne(userId)).thenReturn(user1);
    when(facilityRepository.findOne(homeFacilityId)).thenReturn(homeFacility);
  }

  @Test
  public void shouldGetAllUsers() {
    //given
    Set<UserDto> expectedUserDtos = Sets.newHashSet(user1Dto, user2Dto);
    when(repository.findAll()).thenReturn(users);

    //when
    Set<UserDto> userDtos = controller.getAllUsers();

    //then
    assertEquals(expectedUserDtos, userDtos);
  }

  @Test
  public void shouldGetUser() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleAssignmentRepository.findByUser(userId)).thenReturn(Collections.emptySet());

    //when
    UserDto userDto = controller.getUser(userId);

    //then
    assertEquals(user1Dto, userDto);
  }

  @Test
  public void shouldGetUserWithRoles() {
    //given
    DirectRoleAssignment roleAssignment1 = new DirectRoleAssignment(adminRole1, user1);
    SupervisionRoleAssignment roleAssignment2 = new SupervisionRoleAssignment(supervisionRole1,
        user1, program1);
    user1.assignRoles(roleAssignment1, roleAssignment2);
    when(repository.findOne(userId)).thenReturn(user1);
    when(roleAssignmentRepository.findByUser(userId)).thenReturn(Sets.newHashSet(
        new RoleAssignmentDto(roleId, null, null, null),
        new RoleAssignmentDto(roleId, programId, null, null)));

    UserDto expectedUserDto = new UserDto();
    user1.export(expectedUserDto);

    //when
    UserDto userDto = controller.getUser(userId);

    //then
    assertEquals(expectedUserDto, userDto);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotGetNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    controller.getUser(userId);
  }

  @Test
  public void shouldCreateNewUserOnPut() {
    //given
    preparePostOrPut();


    when(repository.findOne(userId)).thenReturn(null);
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    controller.saveUser(user1Dto, result);

    //then
    verify(repository).save(user1);
  }

  @Test
  public void shouldUpdateExistingUserOnPut() {
    //given
    preparePostOrPut();

    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    controller.saveUser(user1Dto, result);

    //then
    verify(repository).save(user1);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotSaveUserForRoleAssignmentWithoutRole() {
    //given
    preparePostOrPut();

    user1Dto.setRoleAssignments(Sets.newHashSet(new RoleAssignmentDto()));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    controller.saveUser(user1Dto, result);
  }

  @Test
  public void shouldSaveUserWithDirectRole() {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(adminRole1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRole(adminRole1);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    UserDto savedUserDto = controller.saveUser(user1Dto, result);

    //then
    assertEquals(user1Dto, savedUserDto);
    verify(repository).save(user1);
  }

  @Test
  public void shouldSaveUserWithHomeFacilityRole() {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(supervisionRole1);
    when(programRepository.findOne(programId)).thenReturn(program1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRole(supervisionRole1);
    roleAssignmentDto.setProgram(program1);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);


    //when
    UserDto savedUserDto = controller.saveUser(user1Dto, result);

    //then
    assertEquals(user1Dto, savedUserDto);
    verify(repository).save(user1);
  }

  @Test
  public void shouldSaveUserWithSupervisoryRole() {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(supervisionRole1);
    when(programRepository.findOne(programId)).thenReturn(program1);
    when(supervisoryNodeRepository.findOne(supervisoryNodeId)).thenReturn(supervisoryNode1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRole(supervisionRole1);
    roleAssignmentDto.setProgram(program1);
    roleAssignmentDto.setSupervisoryNode(supervisoryNode1);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    UserDto savedUserDto = controller.saveUser(user1Dto, result);

    //then
    assertEquals(user1Dto, savedUserDto);
    verify(repository).save(user1);
  }

  @Test
  public void shouldSaveUserWithFulfillmentRole() {
    //given
    preparePostOrPut();


    when(roleRepository.findOne(roleId)).thenReturn(fulfillmentRole1);
    when(facilityRepository.findOne(warehouseId)).thenReturn(warehouse1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRole(fulfillmentRole1);
    roleAssignmentDto.setWarehouse(warehouse1);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    UserDto savedUserDto = controller.saveUser(user1Dto, result);

    //then
    assertEquals(user1Dto, savedUserDto);
    verify(repository).save(user1);
  }

  @Test
  public void shouldReplaceExistingUserRoles() {
    //given
    preparePostOrPut();

    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1));


    when(roleRepository.findOne(roleId)).thenReturn(adminRole1);
    RoleAssignmentDto roleAssignmentDto = new RoleAssignmentDto();
    roleAssignmentDto.setRole(adminRole1);
    user1Dto.setRoleAssignments(Sets.newHashSet(roleAssignmentDto));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    UserDto savedUserDto = controller.saveUser(user1Dto, result);

    //then
    assertEquals(user1Dto, savedUserDto);
    verify(repository).save(user1);
  }

  @Test
  public void shouldDeleteExistingUserRoles() {
    //given
    preparePostOrPut();


    user1.assignRoles(new SupervisionRoleAssignment(supervisionRole1, user1, program1));
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    //when
    UserDto savedUserDto = controller.saveUser(user1Dto, result);

    //then
    assertEquals(user1Dto, savedUserDto);
    verify(repository).save(user1);
  }

  @Test
  public void shouldDeleteExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(user1);

    //when
    controller.deleteUser(userId);

    //then
    verify(repository).delete(userId);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotDeleteNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    controller.deleteUser(userId);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotCheckIfUserHasRightForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    controller.checkIfUserHasRight( userId,
        UUID.randomUUID(),
        null,
        null,
        null);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotGetUserProgramsForNonExistingUser() {
    //given
    when(repository.findOne(userId)).thenReturn(null);

    //when
    controller.getUserPrograms(userId, true);
  }

  @Test
  public void shouldGetUserFulfillmentFacilities() {
    //given
    FulfillmentRoleAssignment assignment1 =
        new FulfillmentRoleAssignment(fulfillmentRole1, user1, warehouse1);

    SupervisionRoleAssignment assignment2
        = new SupervisionRoleAssignment(supervisionRole1, user1, program1, supervisoryNode1);

    user1.assignRoles(assignment1);
    user1.assignRoles(assignment2);
    when(repository.findOne(userId)).thenReturn(user1);
    when(rightRepository.findOne(rightId)).thenReturn(fulfillmentRight1);

    //when
    Set<FacilityDto> facilities = controller.getUserFulfillmentFacilities(userId, rightId);

    //then
    assertThat(facilities.size(), is(1));
  }

  private void setProgramSupportedAndActive() {
    SupportedProgram supportedProgram =
        SupportedProgram.newSupportedProgram(homeFacility, program1, true);
    program1.setActive(true);
    homeFacility.setSupportedPrograms(Sets.newHashSet(supportedProgram));
  }
}
