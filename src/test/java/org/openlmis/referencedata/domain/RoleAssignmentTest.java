package org.openlmis.referencedata.domain;

import static org.openlmis.referencedata.domain.RightType.GENERAL_ADMIN;
import static org.openlmis.referencedata.domain.RightType.ORDER_FULFILLMENT;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;

import java.util.ArrayList;
import java.util.List;

public class RoleAssignmentTest {

  private String roleName = "role";

  private static class TestStub extends RoleAssignment {
    public TestStub(Role role) throws RightTypeException {
      super(role);
    }

    @Override
    protected List<RightType> getAcceptableRightTypes() {
      List<RightType> acceptableRightTypes = new ArrayList<>();
      acceptableRightTypes.add(GENERAL_ADMIN);
      return acceptableRightTypes;
    }

    @Override
    public boolean hasRight(RightQuery rightQuery) {
      return false;
    }
  }

  @Test
  public void shouldAllowCreationWithMatchingRoleTypes() throws RightTypeException {
    new TestStub(new Role(roleName, Right.ofType(GENERAL_ADMIN)));
  }

  @Test(expected = RightTypeException.class)
  public void shouldNotAllowCreationWithMismatchingRoleTypes() throws RightTypeException {
    new TestStub(new Role(roleName, Right.ofType(ORDER_FULFILLMENT)));
  }
}