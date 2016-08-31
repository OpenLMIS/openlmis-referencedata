package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserTest {
  private RightQuery rightQuery = new RightQuery(new Right("supervisionRight1",
      RightType.SUPERVISION));

  private RoleAssignment assignment1 = mock(RoleAssignment.class);

  private RoleAssignment assignment2 = mock(RoleAssignment.class);

  private User user;

  private String roleName = "role";

  @Before
  public void setUp() {
    user = new User();
  }

  @Test
  public void shouldBeAbleToAssignRoleToUser() throws RightTypeException {
    //when
    user.assignRoles(new DirectRoleAssignment(new Role(roleName, new Right("reportRight1", 
        RightType.REPORTS))));

    //then
    assertThat(user.getRoleAssignments().size(), is(1));
  }

  @Test
  public void shouldHaveRightIfAnyRoleAssignmentHasRight() {
    //given
    user.assignRoles(assignment1);
    user.assignRoles(assignment2);

    when(assignment1.hasRight(rightQuery)).thenReturn(true);
    when(assignment2.hasRight(rightQuery)).thenReturn(false);

    //when
    boolean hasRight = user.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightIfNoRoleAssignmentHasRight() {
    //given
    user.assignRoles(assignment1);
    user.assignRoles(assignment2);

    when(assignment1.hasRight(rightQuery)).thenReturn(false);
    when(assignment2.hasRight(rightQuery)).thenReturn(false);

    //when
    boolean hasRight = user.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }
  
  @Test
  public void shouldGetHomeFacilityPrograms() throws RightTypeException {
    //given
    Role role = new Role(roleName, new Right("right1", RightType.SUPERVISION));
    Program program1 = new Program();
    Program program2 = new Program();

    RoleAssignment assignment3 = new SupervisionRoleAssignment(role, program1);
    RoleAssignment assignment4 = new SupervisionRoleAssignment(role, program2);

    user.assignRoles(assignment3);
    user.assignRoles(assignment4);
    
    //when
    List<Program> programs = user.getHomeFacilityPrograms();
    
    //then
    assertEquals(program1, programs.get(0));
    assertEquals(program2, programs.get(1));
  }

  @Test
  public void shouldGetSupervisedPrograms() throws RightTypeException {
    //given
    Role role = new Role(roleName, new Right("right1", RightType.SUPERVISION));
    Program program1 = new Program();
    Program program2 = new Program();
    SupervisoryNode supervisoryNode = new SupervisoryNode();

    RoleAssignment assignment3 = new SupervisionRoleAssignment(role, program1, supervisoryNode);
    RoleAssignment assignment4 = new SupervisionRoleAssignment(role, program2, supervisoryNode);

    user.assignRoles(assignment3);
    user.assignRoles(assignment4);

    //when
    List<Program> programs = user.getSupervisedPrograms();

    //then
    assertEquals(program1, programs.get(0));
    assertEquals(program2, programs.get(1));
  }

  @Test
  public void shouldGetSupervisedFacilities() throws RightTypeException {
    //given
    SupervisoryNode districtNode = SupervisoryNode.newSupervisoryNode(new Facility());
    RequisitionGroup districtGroup = RequisitionGroup.newRequisitionGroup(districtNode, null,
        Collections.singletonList(new Facility()));
    districtNode.assignRequisitionGroup(districtGroup);

    SupervisoryNode provinceNode = SupervisoryNode.newSupervisoryNode(new Facility());
    RequisitionGroup provinceGroup = RequisitionGroup.newRequisitionGroup(provinceNode, null,
        Arrays.asList(new Facility(), new Facility()));
    provinceNode.assignRequisitionGroup(provinceGroup);
    provinceNode.addChildNode(districtNode);

    Role role = new Role(roleName, new Right("right1", RightType.SUPERVISION));
    Program program = new Program();

    RoleAssignment assignment = new SupervisionRoleAssignment(role, program, provinceNode);

    user.assignRoles(assignment);

    //when
    List<Facility> facilities = user.getSupervisedFacilities();

    //then
    assertThat(facilities.size(), is(3));
  }
}
