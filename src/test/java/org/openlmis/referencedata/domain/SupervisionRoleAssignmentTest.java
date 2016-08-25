package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;

public class SupervisionRoleAssignmentTest {

  private Right right = Right.ofType(SUPERVISION);
  private Program program = new Program();
  SupervisoryNode node = new SupervisoryNode();
  private String roleName = "role";
  SupervisionRoleAssignment supervisionRoleAssignment =
      new SupervisionRoleAssignment(new Role(roleName, right), program, node);

  public SupervisionRoleAssignmentTest() throws RightTypeException {
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndSupervisoryNodeMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, node);
    boolean hasRight = supervisionRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenProgramDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, new Program(), node);
    boolean hasRight = supervisionRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenNodeDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, new SupervisoryNode());
    boolean hasRight = supervisionRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }
}
