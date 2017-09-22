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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.dto.RoleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.service.RightAssignmentService;
import org.openlmis.referencedata.service.RightService;
import org.springframework.dao.DataIntegrityViolationException;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
public class RoleControllerTest {

  @Mock
  private RoleRepository repository;

  @Mock
  private RightRepository rightRepository;

  @Mock
  private RightService rightService;
  
  @Mock
  private RightAssignmentService rightAssignmentService;

  @InjectMocks
  private RoleController controller = new RoleController();

  private String right1Name;
  private Right right1;
  private String right2Name;
  private Right right2;
  private String right3Name;
  private Right right3;
  private String role1Name;
  private Role role1;
  private List<Role> roles;
  private RoleDto role1Dto;
  private UUID roleId;

  /**
   * Constructor for test.
   */
  public RoleControllerTest() {
    initMocks(this);

    right1Name = "right1";
    right1 = Right.newRight(right1Name, RightType.GENERAL_ADMIN);
    right2Name = "right2";
    right2 = Right.newRight(right2Name, RightType.GENERAL_ADMIN);

    role1Name = "role1";
    role1 = Role.newRole(role1Name, right1, right2);
    roles = Collections.singletonList(role1);

    role1Dto = new RoleDto();
    role1.export(role1Dto);
    roleId = UUID.randomUUID();
  }
  
  private void preparePostOrPut() {
    when(repository.findFirstByName(role1Name)).thenReturn(role1);
    when(rightRepository.findFirstByName(right1Name)).thenReturn(right1);
    when(rightRepository.findFirstByName(right2Name)).thenReturn(right2);
  }

  @Test
  public void shouldGetAllRoles() {
    //given
    Set<RoleDto> expectedRoleDtos = Sets.newHashSet(role1Dto);
    when(repository.findAll()).thenReturn(roles);

    //when
    Set<RoleDto> roleDtos = controller.getAllRoles();

    //then
    assertEquals(expectedRoleDtos, roleDtos);
  }

  @Test
  public void shouldGetRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(role1);

    //when
    RoleDto roleDto = controller.getRole(roleId);

    //then
    assertEquals(role1Dto, roleDto);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotGetNonExistingRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(null);

    //when
    controller.getRole(roleId);
  }

  @Test
  public void shouldCreateNewRoleOnPost() {
    //given
    preparePostOrPut();

    when(repository.findFirstByName(role1Name)).thenReturn(null);

    //when
    controller.createRole(role1Dto);

    //then
    verify(repository).save(role1);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotCreateExistingRoleOnPost() {
    //given
    preparePostOrPut();

    //when
    controller.createRole(role1Dto);
  }

  @Test
  public void shouldUpdateRoleOnPut() {
    //given
    preparePostOrPut();

    role1Dto.setName("updatedRole1");
    Role updatedRole1 = Role.newRole(role1Dto);

    //when
    controller.updateRole(roleId, role1Dto);

    //then
    verify(repository).saveAndFlush(updatedRole1);
    verify(rightAssignmentService).regenerateRightAssignments();
  }

  @Test
  public void shouldCreateNewRoleOnPut() {
    //given
    preparePostOrPut();

    when(repository.findFirstByName(role1Name)).thenReturn(null);
    role1Dto.setName("updatedRole1");
    Role updatedRole1 = Role.newRole(role1Dto);

    //when
    controller.updateRole(roleId, role1Dto);

    //then
    verify(repository).saveAndFlush(updatedRole1);
    verify(rightAssignmentService).regenerateRightAssignments();
  }

  @Test
  public void shouldAddRoleRightsOnPut() {
    //given
    preparePostOrPut();

    right3Name = "right3";
    right3 = Right.newRight(right3Name, RightType.GENERAL_ADMIN);
    when(rightRepository.findFirstByName(right3Name)).thenReturn(right3);
    Role updatedRole1 = Role.newRole(role1Name, right1, right2, right3);
    RoleDto updatedRole1Dto = new RoleDto();
    updatedRole1.export(updatedRole1Dto);

    //when
    controller.updateRole(roleId, updatedRole1Dto);

    //then
    verify(repository).saveAndFlush(updatedRole1);
    verify(rightAssignmentService).regenerateRightAssignments();
  }

  @Test
  public void shouldUpdateRoleRightsOnPut() {
    //given
    preparePostOrPut();

    right3Name = "right3";
    right3 = Right.newRight(right3Name, RightType.GENERAL_ADMIN);
    when(rightRepository.findFirstByName(right3Name)).thenReturn(right3);
    Role updatedRole1 = Role.newRole(role1Name, right1, right3);
    RoleDto updatedRole1Dto = new RoleDto();
    updatedRole1.export(updatedRole1Dto);

    //when
    controller.updateRole(roleId, updatedRole1Dto);

    //then
    verify(repository).saveAndFlush(updatedRole1);
    verify(rightAssignmentService).regenerateRightAssignments();
  }

  @Test
  public void shouldDeleteRoleRightsOnPut() {
    //given
    preparePostOrPut();

    Role updatedRole1 = Role.newRole(role1Name, right1);
    RoleDto updatedRole1Dto = new RoleDto();
    updatedRole1.export(updatedRole1Dto);

    //when
    controller.updateRole(roleId, updatedRole1Dto);

    //then
    verify(repository).saveAndFlush(updatedRole1);
    verify(rightAssignmentService).regenerateRightAssignments();
  }

  @Test
  public void shouldDeleteExistingRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(role1);

    //when
    controller.deleteRole(roleId);

    //then
    verify(repository).delete(roleId);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotDeleteNonExistingRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(null);

    //when
    controller.deleteRole(roleId);
  }
}
