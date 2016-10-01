package org.openlmis.referencedata.domain;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserTest {
  private RightQuery rightQuery = new RightQuery(Right.newRight("supervisionRight1",
      RightType.SUPERVISION));

  private RoleAssignment assignment1 = mock(RoleAssignment.class);

  private RoleAssignment assignment2 = mock(RoleAssignment.class);

  private User user;

  private String roleName = "role";

  @Before
  public void setUp() {
    user = new UserBuilder("user", "Test", "User", "test@test.com").createUser();
  }

  @Test
  public void shouldBeAbleToAssignRoleToUser() throws RightTypeException, RoleException {
    //when
    user.assignRoles(new DirectRoleAssignment(Role.newRole(roleName, Right.newRight("reportRight1",
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
  public void shouldGetHomeFacilityPrograms() throws RightTypeException, RoleException {
    //given
    Role role = Role.newRole(roleName, Right.newRight("right1", RightType.SUPERVISION));
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
  public void shouldGetSupervisedPrograms() throws RightTypeException, RoleException {
    //given
    Role role = Role.newRole(roleName, Right.newRight("right1", RightType.SUPERVISION));
    Program program1 = new Program("prog1");
    Program program2 = new Program("prog2");
    SupervisoryNode supervisoryNode =
        SupervisoryNode.newSupervisoryNode("SN1", new Facility("C1"));

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
  public void shouldGetSupervisedFacilities() throws RightTypeException, RoleException {
    //given
    SupervisoryNode districtNode = SupervisoryNode.newSupervisoryNode("DN", new Facility("C1"));
    RequisitionGroup districtGroup = new RequisitionGroup("DG", "DGN", districtNode);
    districtGroup.setMemberFacilities(Collections.singletonList(new Facility("C2")));
    districtNode.setRequisitionGroup(districtGroup);

    SupervisoryNode provinceNode = SupervisoryNode.newSupervisoryNode("PN", new Facility("C3"));
    RequisitionGroup provinceGroup = new RequisitionGroup("PG", "PGN", provinceNode);
    provinceGroup.setMemberFacilities(Arrays.asList(new Facility("C4"), new Facility("C5")));
    provinceNode.setRequisitionGroup(provinceGroup);

    districtNode.assignParentNode(provinceNode);

    Role role = Role.newRole(roleName, Right.newRight("right1", RightType.SUPERVISION));
    Program program = new Program("prog1");

    RoleAssignment assignment = new SupervisionRoleAssignment(role, program, provinceNode);

    user.assignRoles(assignment);

    //when
    Set<Facility> facilities = user.getSupervisedFacilities();

    //then
    assertThat(facilities.size(), is(3));
  }

  @Test
  public void shouldGetFulfillmentFacilities() {
    //given
    FulfillmentRoleAssignment fulfillmentRoleAssignment = mock(FulfillmentRoleAssignment.class);

    user.assignRoles(fulfillmentRoleAssignment);
    user.assignRoles(assignment1);
    user.assignRoles(assignment2);

    //when
    Set<Facility> facilities = user.getFulfillmentFacilities();

    //then
    assertThat(facilities.size(), is(1));
  }
}
