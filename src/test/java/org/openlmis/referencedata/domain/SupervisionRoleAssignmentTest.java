package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;

import java.util.Set;

public class SupervisionRoleAssignmentTest {

  private Right right = new Right("right", SUPERVISION);
  private Program program = new Program();
  private SupervisoryNode node = new SupervisoryNode();
  private Role role = new Role("role", right);
  private SupervisionRoleAssignment homeFacilityRoleAssignment =
      new SupervisionRoleAssignment(role, program);
  private SupervisionRoleAssignment supervisedRoleAssignment =
      new SupervisionRoleAssignment(role, program, node);
  private User user = new User();

  public SupervisionRoleAssignmentTest() throws RightTypeException {
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndSupervisoryNodeMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, node);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenProgramDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, new Program(), node);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenNodeDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, new SupervisoryNode());
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
