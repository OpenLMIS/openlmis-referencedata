package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.ORDER_FULFILLMENT;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleAssignmentException;

public class FulfillmentRoleAssignmentTest {

  private Right right;
  private Facility warehouse;
  private Facility hospital;
  private FulfillmentRoleAssignment fulfillmentRoleAssignment;
  private String roleName = "role";

  /**
   * Setup constructor.
   */
  public FulfillmentRoleAssignmentTest() throws RightTypeException, RoleAssignmentException {
    right = Right.newRight("right", ORDER_FULFILLMENT);
    warehouse = new Facility();
    warehouse.setType(new FacilityType("warehouse"));
    hospital = new Facility();
    hospital.setType(new FacilityType("hospital"));
    fulfillmentRoleAssignment = new FulfillmentRoleAssignment(
        Role.newRole(roleName, right), warehouse);
  }

  @Test
  public void shouldAllowCreationWithWarehouseFacilityType()
      throws RightTypeException, RoleAssignmentException {
    new FulfillmentRoleAssignment(Role.newRole(roleName, right), warehouse);
  }

  @Test(expected = RoleAssignmentException.class)
  public void shouldNotAllowCreationWithNonWarehouseFacilityType()
      throws RightTypeException, RoleAssignmentException {
    new FulfillmentRoleAssignment(Role.newRole(roleName, right), hospital);
  }

  @Test
  public void shouldHaveRightWhenRightAndFacilityMatch()
      throws RightTypeException, RoleAssignmentException {
    //when
    RightQuery rightQuery = new RightQuery(right, warehouse);
    boolean hasRight = fulfillmentRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenFacilityDoesNotMatch()
      throws RightTypeException, RoleAssignmentException {
    //when
    RightQuery rightQuery = new RightQuery(right, hospital);
    boolean hasRight = fulfillmentRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }
}
