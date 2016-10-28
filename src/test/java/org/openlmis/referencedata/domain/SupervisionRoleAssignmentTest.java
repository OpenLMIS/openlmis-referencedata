package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.Set;

public class SupervisionRoleAssignmentTest {

  private Right right;
  private Role role;
  private Program program;
  private Facility homeFacility;
  private SupervisionRoleAssignment homeFacilityRoleAssignment;
  private SupervisoryNode supervisoryNode;
  private Facility supervisedFacility;
  private SupervisionRoleAssignment supervisedRoleAssignment;
  private User user;

  /**
   * Constructor for tests.
   */
  public SupervisionRoleAssignmentTest() throws RightTypeException, RoleException {
    right = Right.newRight("right", SUPERVISION);
    role = Role.newRole("role", right);
    program = new Program("em");

    homeFacility = new Facility("F1");
    homeFacilityRoleAssignment = new SupervisionRoleAssignment(role, program, homeFacility);

    supervisoryNode = SupervisoryNode.newSupervisoryNode("SN1", new Facility("F2"));
    RequisitionGroup requisitionGroup = new RequisitionGroup("RG1", "RGN1", supervisoryNode);
    supervisedFacility = new Facility("F2");
    requisitionGroup.setMemberFacilities(Sets.newHashSet(supervisedFacility));
    supervisoryNode.setRequisitionGroup(requisitionGroup);
    supervisedRoleAssignment = new SupervisionRoleAssignment(role, program, supervisoryNode);

    user = new UserBuilder("testuser", "Test", "User", "test@test.com").createUser();
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndHomeFacilityMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, homeFacility);
    boolean hasRight = homeFacilityRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndSupervisedFacilityMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, supervisedFacility);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenRightDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(Right.newRight("right2", SUPERVISION), program,
        homeFacility);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenProgramDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, new Program("test"), homeFacility);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenFacilityDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, new Facility("Another"));
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldAssignHomeFacilityProgramWhenUserAssignedWithNoNode() {

    //when
    homeFacilityRoleAssignment.assignTo(user);
    Set<Program> programs = user.getHomeFacilityPrograms();

    //then
    assertThat(programs.size(), is(1));
    assertTrue(programs.contains(program));
  }

  @Test
  public void shouldAssignSupervisedProgramWhenUserAssignedWithNode() {

    //when
    supervisedRoleAssignment.assignTo(user);
    Set<Program> programs = user.getSupervisedPrograms();

    //then
    assertThat(programs.size(), is(1));
    assertTrue(programs.contains(program));
  }
}
