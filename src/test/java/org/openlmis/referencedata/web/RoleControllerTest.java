package org.openlmis.referencedata.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.Sets;

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
import org.openlmis.referencedata.service.RightService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
public class RoleControllerTest {

  @Mock
  private RoleRepository repository;

  @Mock
  private RightRepository rightRepository;

  @Mock
  private RightService rightService;

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
  private Set<Role> roles;
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
    roles = Sets.newHashSet(role1);

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
    ResponseEntity responseEntity = controller.getAllRoles();
    HttpStatus httpStatus = responseEntity.getStatusCode();
    Set<RoleDto> roleDtos = (Set<RoleDto>) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(expectedRoleDtos, roleDtos);
  }

  @Test
  public void shouldGetRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(role1);

    //when
    ResponseEntity responseEntity = controller.getRole(roleId);
    HttpStatus httpStatus = responseEntity.getStatusCode();
    RoleDto roleDto = (RoleDto) responseEntity.getBody();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    assertEquals(role1Dto, roleDto);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotGetNonExistingRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(null);

    //when
    controller.getRole(roleId).getStatusCode();
  }

  @Test
  public void shouldCreateNewRoleOnPost() {
    //given
    preparePostOrPut();

    when(repository.findFirstByName(role1Name)).thenReturn(null);

    //when
    HttpStatus httpStatus = controller.createRole(role1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.CREATED));
    verify(repository).save(role1);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotCreateExistingRoleOnPost() {
    //given
    preparePostOrPut();

    //when
    controller.createRole(role1Dto).getStatusCode();
  }

  @Test
  public void shouldUpdateRoleOnPut() {
    //given
    preparePostOrPut();

    role1Dto.setName("updatedRole1");
    Role updatedRole1 = Role.newRole(role1Dto);

    //when
    HttpStatus httpStatus = controller.updateRole(roleId, role1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(repository).save(updatedRole1);
  }

  @Test
  public void shouldCreateNewRoleOnPut() {
    //given
    preparePostOrPut();

    when(repository.findFirstByName(role1Name)).thenReturn(null);
    role1Dto.setName("updatedRole1");
    Role updatedRole1 = Role.newRole(role1Dto);

    //when
    HttpStatus httpStatus = controller.updateRole(roleId, role1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(repository).save(updatedRole1);
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
    HttpStatus httpStatus = controller.updateRole(roleId, updatedRole1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(repository).save(updatedRole1);
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
    HttpStatus httpStatus = controller.updateRole(roleId, updatedRole1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(repository).save(updatedRole1);
  }

  @Test
  public void shouldDeleteRoleRightsOnPut() {
    //given
    preparePostOrPut();

    Role updatedRole1 = Role.newRole(role1Name, right1);
    RoleDto updatedRole1Dto = new RoleDto();
    updatedRole1.export(updatedRole1Dto);

    //when
    HttpStatus httpStatus = controller.updateRole(roleId, updatedRole1Dto).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.OK));
    verify(repository).save(updatedRole1);
  }

  @Test
  public void shouldDeleteExistingRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(role1);

    //when
    HttpStatus httpStatus = controller.deleteRole(roleId).getStatusCode();

    //then
    assertThat(httpStatus, is(HttpStatus.NO_CONTENT));
    verify(repository).delete(roleId);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotDeleteNonExistingRole() {
    //given
    when(repository.findOne(roleId)).thenReturn(null);

    //when
    controller.deleteRole(roleId).getStatusCode();
  }
}
