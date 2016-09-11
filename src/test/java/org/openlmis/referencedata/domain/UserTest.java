package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
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
import java.util.Set;

public class UserTest {
  private RightQuery rightQuery = new RightQuery(Right.newRight("supervisionRight1",
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
    user.assignRoles(new DirectRoleAssignment(new Role(roleName, Right.newRight("reportRight1",
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
    Role role = new Role(roleName, Right.newRight("right1", RightType.SUPERVISION));
    Program program1 = new Program("prog1");
    Program program2 = new Program("prog2");

    RoleAssignment assignment3 = new SupervisionRoleAssignment(role, program1);
    RoleAssignment assignment4 = new SupervisionRoleAssignment(role, program2);

    user.assignRoles(assignment3);
    user.assignRoles(assignment4);

    //when
    Set<Program> programs = user.getHomeFacilityPrograms();

    //then
    assertTrue(programs.contains(program1));
    assertTrue(programs.contains(program2));
  }

  @Test
  public void shouldGetSupervisedPrograms() throws RightTypeException {
    //given
    Role role = new Role(roleName, Right.newRight("right1", RightType.SUPERVISION));
    Program program1 = new Program("prog1");
    Program program2 = new Program("prog2");
    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode("SN1", new Facility());

    RoleAssignment assignment3 = new SupervisionRoleAssignment(role, program1, supervisoryNode);
    RoleAssignment assignment4 = new SupervisionRoleAssignment(role, program2, supervisoryNode);

    user.assignRoles(assignment3);
    user.assignRoles(assignment4);

    //when
    Set<Program> programs = user.getSupervisedPrograms();

    //then
    assertTrue(programs.contains(program1));
    assertTrue(programs.contains(program2));
  }

  @Test
  public void shouldGetSupervisedFacilities() throws RightTypeException {
    //given
    SupervisoryNode districtNode = SupervisoryNode.newSupervisoryNode("DN", new Facility());
    RequisitionGroup districtGroup = RequisitionGroup.newRequisitionGroup("DG", districtNode);
    districtGroup.setMemberFacilities(Collections.singletonList(new Facility()));
    districtNode.setRequisitionGroup(districtGroup);

    SupervisoryNode provinceNode = SupervisoryNode.newSupervisoryNode("PN", new Facility());
    RequisitionGroup provinceGroup = RequisitionGroup.newRequisitionGroup("PG", provinceNode);
    provinceGroup.setMemberFacilities(Arrays.asList(new Facility(), new Facility()));
    provinceNode.setRequisitionGroup(provinceGroup);

    districtNode.assignParentNode(provinceNode);

    Role role = new Role(roleName, Right.newRight("right1", RightType.SUPERVISION));
    Program program = new Program("prog1");

    RoleAssignment assignment = new SupervisionRoleAssignment(role, program, provinceNode);

    user.assignRoles(assignment);

    //when
    Set<Facility> facilities = user.getSupervisedFacilities();

    //then
    assertThat(facilities.size(), is(3));
  }
}
