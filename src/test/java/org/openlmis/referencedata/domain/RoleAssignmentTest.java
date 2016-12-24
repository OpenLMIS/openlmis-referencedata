package org.openlmis.referencedata.domain;

import static org.mockito.Mockito.mock;
import static org.openlmis.referencedata.domain.RightType.GENERAL_ADMIN;
import static org.openlmis.referencedata.domain.RightType.ORDER_FULFILLMENT;

import org.junit.Test;
import org.openlmis.referencedata.exception.AuthException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.HashSet;
import java.util.Set;

public class RoleAssignmentTest {

  private String roleName = "role";

  private static class TestStub extends RoleAssignment {
    public TestStub(Role role, User user) {
      super(role, user);
    }

    @Override
    protected Set<RightType> getAcceptableRightTypes() {
      Set<RightType> acceptableRightTypes = new HashSet<>();
      acceptableRightTypes.add(GENERAL_ADMIN);
      return acceptableRightTypes;
    }

    @Override
    public boolean hasRight(RightQuery rightQuery) {
      return false;
    }
  }

  @Test
  public void shouldAllowCreationWithMatchingRoleTypes() throws RoleException {
    new TestStub(Role.newRole(roleName, Right.newRight("adminRight1", GENERAL_ADMIN)),
        mock(User.class));
  }

  @Test(expected = AuthException.class)
  public void shouldNotAllowCreationWithMismatchingRoleTypes() throws RoleException {
    new TestStub(Role.newRole(roleName, Right.newRight("fulfillmentRight1", ORDER_FULFILLMENT)),
        mock(User.class));
  }
}